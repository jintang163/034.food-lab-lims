package com.foodlab.task.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodlab.common.constant.MQConstants;
import com.foodlab.common.constant.RedisConstants;
import com.foodlab.common.constant.TaskConstants;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.common.result.ResultCode;
import com.foodlab.common.utils.CodeGenerator;
import com.foodlab.common.utils.JsonUtils;
import com.foodlab.sample.entity.Sample;
import com.foodlab.sample.service.SampleService;
import com.foodlab.task.dto.TaskAssignDTO;
import com.foodlab.task.dto.TaskQueryDTO;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.entity.TaskFlowLog;
import com.foodlab.task.mapper.DetectTaskMapper;
import com.foodlab.task.mapper.TaskFlowLogMapper;
import com.foodlab.task.service.DetectTaskService;
import com.foodlab.task.vo.TaskDetailVO;
import com.foodlab.task.vo.TaskFlowLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetectTaskServiceImpl extends ServiceImpl<DetectTaskMapper, DetectTask> implements DetectTaskService {

    private final TaskFlowLogMapper taskFlowLogMapper;
    private final SampleService sampleService;
    private final StringRedisTemplate redisTemplate;
    private final RocketMQTemplate rocketMQTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DetectTask assignTask(TaskAssignDTO dto, Long assignUserId, String assignUserName) {
        Sample sample = sampleService.getById(dto.getSampleId());
        if (sample == null) {
            throw new BusinessException(ResultCode.SAMPLE_NOT_FOUND);
        }

        String taskCode = CodeGenerator.generateTaskCode();

        DetectTask task = new DetectTask();
        task.setTaskCode(taskCode);
        task.setSampleId(dto.getSampleId());
        task.setSampleCode(sample.getSampleCode());
        task.setTaskName(dto.getTaskName());
        task.setTaskType(dto.getTaskType() != null ? dto.getTaskType() : TaskConstants.TASK_TYPE_NORMAL);
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : TaskConstants.TASK_PRIORITY_MEDIUM);
        task.setTaskStatus(TaskConstants.TASK_STATUS_ASSIGNED);
        task.setAssignBy(assignUserId);
        task.setAssignByName(assignUserName);
        task.setAssignTime(LocalDateTime.now());
        task.setDetectPersonId(dto.getDetectPersonId());
        task.setRemark(dto.getRemark());
        task.setDetectItemCount(sample.getDetectItemCount());
        task.setCompletedItemCount(0);

        save(task);

        saveFlowLog(task.getId(), taskCode, "任务分配", "ASSIGN", "分配任务",
                assignUserId, assignUserName, null, TaskConstants.TASK_STATUS_ASSIGNED, dto.getRemark());

        sampleService.updateSampleStatus(dto.getSampleId(), com.foodlab.common.constant.SampleConstants.SAMPLE_STATUS_DETECTING);

        Map<String, Object> msg = new HashMap<>();
        msg.put("taskId", task.getId());
        msg.put("taskCode", taskCode);
        msg.put("detectPersonId", dto.getDetectPersonId());
        msg.put("sampleName", sample.getSampleName());
        rocketMQTemplate.convertAndSend(MQConstants.TASK_ASSIGN_TOPIC,
                MessageBuilder.withPayload(JsonUtils.toJson(msg)).build());

        return task;
    }

    @Override
    public TaskDetailVO getTaskDetail(Long taskId) {
        String cacheKey = RedisConstants.TASK_CACHE_KEY + taskId;
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(cacheValue)) {
            return JsonUtils.parse(cacheValue, TaskDetailVO.class);
        }

        DetectTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        TaskDetailVO vo = BeanUtil.copyProperties(task, TaskDetailVO.class);

        List<TaskFlowLog> flowLogs = taskFlowLogMapper.selectByTaskId(taskId);
        List<TaskFlowLogVO> flowLogVOS = flowLogs.stream()
                .map(log -> BeanUtil.copyProperties(log, TaskFlowLogVO.class))
                .collect(Collectors.toList());
        vo.setFlowLogs(flowLogVOS);

        redisTemplate.opsForValue().set(cacheKey, JsonUtils.toJson(vo),
                RedisConstants.DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);

        return vo;
    }

    @Override
    public IPage<DetectTask> getTaskPage(int pageNum, int pageSize, TaskQueryDTO queryDTO) {
        Page<DetectTask> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectTaskPage(page, queryDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startTask(Long taskId, Long userId) {
        DetectTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        if (!TaskConstants.TASK_STATUS_ASSIGNED.equals(task.getTaskStatus())) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR);
        }
        if (!task.getDetectPersonId().equals(userId)) {
            throw new BusinessException(ResultCode.FAIL, "只能开始自己的任务");
        }

        String fromStatus = task.getTaskStatus();
        task.setTaskStatus(TaskConstants.TASK_STATUS_DETECTING);
        task.setStartTime(LocalDateTime.now());
        boolean result = updateById(task);

        saveFlowLog(task.getId(), task.getTaskCode(), "开始检测", "START", "开始检测",
                userId, task.getDetectPersonName(), fromStatus, TaskConstants.TASK_STATUS_DETECTING, null);

        evictTaskCache(taskId);
        sendStatusChangeMessage(task);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitTask(Long taskId, Long userId) {
        DetectTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        if (!TaskConstants.TASK_STATUS_DETECTING.equals(task.getTaskStatus())) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR);
        }

        String fromStatus = task.getTaskStatus();
        task.setTaskStatus(TaskConstants.TASK_STATUS_FIRST_AUDIT);
        task.setEndTime(LocalDateTime.now());
        boolean result = updateById(task);

        saveFlowLog(task.getId(), task.getTaskCode(), "提交审核", "SUBMIT", "提交一级审核",
                userId, task.getDetectPersonName(), fromStatus, TaskConstants.TASK_STATUS_FIRST_AUDIT, null);

        evictTaskCache(taskId);
        sendStatusChangeMessage(task);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTaskStatus(Long taskId, String status, Long operatorId, String operatorName, String remark) {
        DetectTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        String fromStatus = task.getTaskStatus();
        task.setTaskStatus(status);
        boolean result = updateById(task);

        String nodeName = "状态变更";
        String nodeCode = "STATUS_CHANGE";
        String operationType = "状态变更";

        switch (status) {
            case TaskConstants.TASK_STATUS_FIRST_AUDIT:
                nodeName = "一级审核";
                nodeCode = "FIRST_AUDIT";
                operationType = "进入一级审核";
                break;
            case TaskConstants.TASK_STATUS_SECOND_AUDIT:
                nodeName = "二级审核";
                nodeCode = "SECOND_AUDIT";
                operationType = "进入二级审核";
                break;
            case TaskConstants.TASK_STATUS_COMPLETED:
                nodeName = "任务完成";
                nodeCode = "COMPLETED";
                operationType = "任务完成";
                break;
            case TaskConstants.TASK_STATUS_REJECTED:
                nodeName = "审核驳回";
                nodeCode = "REJECTED";
                operationType = "审核驳回";
                break;
        }

        saveFlowLog(taskId, task.getTaskCode(), nodeName, nodeCode, operationType,
                operatorId, operatorName, fromStatus, status, remark);

        evictTaskCache(taskId);
        sendStatusChangeMessage(task);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTask(Long taskId, Long userId) {
        DetectTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        if (TaskConstants.TASK_STATUS_DETECTING.equals(task.getTaskStatus())
                || TaskConstants.TASK_STATUS_COMPLETED.equals(task.getTaskStatus())) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR);
        }

        String fromStatus = task.getTaskStatus();
        task.setTaskStatus(TaskConstants.TASK_STATUS_PENDING);
        task.setDetectPersonId(null);
        task.setDetectPersonName(null);
        task.setAssignTime(null);
        boolean result = updateById(task);

        saveFlowLog(taskId, task.getTaskCode(), "取消任务", "CANCEL", "取消分配",
                userId, null, fromStatus, TaskConstants.TASK_STATUS_PENDING, null);

        evictTaskCache(taskId);
        sendStatusChangeMessage(task);

        return result;
    }

    @Override
    public List<DetectTask> getMyTasks(Long userId, String status) {
        LambdaQueryWrapper<DetectTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DetectTask::getDetectPersonId, userId);
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(DetectTask::getTaskStatus, status);
        }
        wrapper.orderByDesc(DetectTask::getPriority);
        wrapper.orderByDesc(DetectTask::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean autoAssign(Long sampleId, Long detectItemId) {
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeTask(Long taskId, Long userId) {
        DetectTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        if (!TaskConstants.TASK_STATUS_IN_PROGRESS.equals(task.getTaskStatus())
                && !TaskConstants.TASK_STATUS_DETECTING.equals(task.getTaskStatus())) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR, "任务状态不正确，无法完成");
        }

        String fromStatus = task.getTaskStatus();
        task.setTaskStatus(TaskConstants.TASK_STATUS_SUBMITTED);
        task.setEndTime(LocalDateTime.now());
        boolean result = updateById(task);

        saveFlowLog(taskId, task.getTaskCode(), "检测完成", "COMPLETE", "完成检测并提交审核",
                userId, task.getDetectPersonName(), fromStatus, TaskConstants.TASK_STATUS_SUBMITTED, null);

        evictTaskCache(taskId);
        sendStatusChangeMessage(task);

        return result;
    }

    private void saveFlowLog(Long taskId, String taskCode, String nodeName, String nodeCode,
                             String operationType, Long operatorId, String operatorName,
                             String fromStatus, String toStatus, String remark) {
        TaskFlowLog flowLog = new TaskFlowLog();
        flowLog.setTaskId(taskId);
        flowLog.setTaskCode(taskCode);
        flowLog.setNodeName(nodeName);
        flowLog.setNodeCode(nodeCode);
        flowLog.setOperationType(operationType);
        flowLog.setOperatorId(operatorId);
        flowLog.setOperatorName(operatorName);
        flowLog.setOperateTime(LocalDateTime.now());
        flowLog.setFromStatus(fromStatus);
        flowLog.setToStatus(toStatus);
        flowLog.setRemark(remark);
        taskFlowLogMapper.insert(flowLog);
    }

    private void evictTaskCache(Long taskId) {
        String cacheKey = RedisConstants.TASK_CACHE_KEY + taskId;
        redisTemplate.delete(cacheKey);
    }

    private void sendStatusChangeMessage(DetectTask task) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("taskId", task.getId());
            msg.put("taskCode", task.getTaskCode());
            msg.put("taskStatus", task.getTaskStatus());
            msg.put("sampleId", task.getSampleId());
            rocketMQTemplate.convertAndSend(MQConstants.TASK_STATUS_CHANGE_TOPIC,
                    MessageBuilder.withPayload(JsonUtils.toJson(msg)).build());
        } catch (Exception e) {
            log.error("发送任务状态变更消息失败", e);
        }
    }
}
