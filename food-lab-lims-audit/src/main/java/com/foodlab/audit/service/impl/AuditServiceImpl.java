package com.foodlab.audit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodlab.audit.dto.AuditQueryDTO;
import com.foodlab.audit.dto.AuditSubmitDTO;
import com.foodlab.audit.dto.SamplingReviewDTO;
import com.foodlab.audit.entity.AuditRecord;
import com.foodlab.audit.entity.SamplingReview;
import com.foodlab.audit.mapper.AuditRecordMapper;
import com.foodlab.audit.mapper.SamplingReviewMapper;
import com.foodlab.audit.service.AuditService;
import com.foodlab.audit.vo.AuditFlowVO;
import com.foodlab.audit.vo.AuditRecordVO;
import com.foodlab.common.constant.AuditConstants;
import com.foodlab.common.constant.DetectConstants;
import com.foodlab.common.constant.TaskConstants;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.common.result.ResultCode;
import com.foodlab.common.utils.CodeGenerator;
import com.foodlab.detect.entity.DetectResult;
import com.foodlab.detect.mapper.DetectResultMapper;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.mapper.DetectTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuditServiceImpl extends ServiceImpl<AuditRecordMapper, AuditRecord> implements AuditService {

    @Autowired
    private AuditRecordMapper auditRecordMapper;

    @Autowired
    private DetectTaskMapper detectTaskMapper;

    @Autowired
    private DetectResultMapper detectResultMapper;

    @Autowired
    private SamplingReviewMapper samplingReviewMapper;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitAudit(AuditSubmitDTO dto, Long userId, String userName) {
        AuditRecord currentRecord = auditRecordMapper.selectLatestByBusinessAndLevel(
                dto.getBusinessType(), dto.getBusinessId(), dto.getAuditLevel());

        if (currentRecord == null) {
            throw new BusinessException(ResultCode.AUDIT_NOT_FOUND);
        }

        if (!AuditConstants.AUDIT_STATUS_PENDING.equals(currentRecord.getAuditStatus())) {
            throw new BusinessException(ResultCode.AUDIT_STATUS_ERROR, "当前审核状态不允许操作");
        }

        boolean isPass = AuditConstants.AUDIT_RESULT_PASS.equals(dto.getAuditResult());
        boolean isRetest = AuditConstants.AUDIT_RESULT_RETEST.equals(dto.getAuditResult());

        currentRecord.setAuditStatus(isPass ? AuditConstants.AUDIT_STATUS_PASS
                : isRetest ? AuditConstants.AUDIT_STATUS_RETEST
                : AuditConstants.AUDIT_STATUS_REJECT);
        currentRecord.setAuditorId(userId);
        currentRecord.setAuditorName(userName);
        currentRecord.setAuditTime(LocalDateTime.now());
        currentRecord.setAuditOpinion(dto.getAuditOpinion());
        currentRecord.setRemark(dto.getRemark());
        currentRecord.setActionType(isPass ? AuditConstants.ACTION_TYPE_APPROVE
                : isRetest ? AuditConstants.ACTION_TYPE_RETEST
                : AuditConstants.ACTION_TYPE_REJECT);
        currentRecord.setUpdateBy(userId);

        updateById(currentRecord);

        if (isPass) {
            if (dto.getAuditLevel() == 1) {
                createSecondLevelAudit(dto, currentRecord.getId(), userId);
                updateBusinessStatus(dto.getBusinessType(), dto.getBusinessId(), TaskConstants.TASK_STATUS_SECOND_AUDIT);
            } else {
                updateBusinessStatus(dto.getBusinessType(), dto.getBusinessId(), TaskConstants.TASK_STATUS_APPROVED);
                updateDetectResultsAuditStatus(dto.getBusinessId(), DetectConstants.IS_AUDIT_YES);
            }
        } else if (isRetest) {
            updateBusinessStatus(dto.getBusinessType(), dto.getBusinessId(), TaskConstants.TASK_STATUS_RETEST);
        } else {
            updateBusinessStatus(dto.getBusinessType(), dto.getBusinessId(), TaskConstants.TASK_STATUS_REJECTED);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startAudit(String businessType, Long businessId, String businessCode, Long creatorId) {
        List<AuditRecord> existing = auditRecordMapper.selectByBusiness(businessType, businessId);
        if (!existing.isEmpty()) {
            existing.forEach(ar -> {
                ar.setDeleted(1);
                updateById(ar);
            });
        }

        AuditRecord firstAudit = new AuditRecord();
        firstAudit.setAuditCode(CodeGenerator.generateAuditCode());
        firstAudit.setBusinessType(businessType);
        firstAudit.setBusinessId(businessId);
        firstAudit.setBusinessCode(businessCode);
        firstAudit.setAuditLevel(1);
        firstAudit.setAuditStatus(AuditConstants.AUDIT_STATUS_PENDING);
        firstAudit.setCreateBy(creatorId);
        firstAudit.setUpdateBy(creatorId);
        save(firstAudit);

        updateBusinessStatus(businessType, businessId, TaskConstants.TASK_STATUS_FIRST_AUDIT);

        return true;
    }

    @Override
    public AuditRecordVO getAuditDetail(Long id) {
        AuditRecord record = getById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.AUDIT_NOT_FOUND);
        }
        AuditRecordVO vo = BeanUtil.copyProperties(record, AuditRecordVO.class);
        return vo;
    }

    @Override
    public List<AuditRecordVO> getAuditHistory(String businessType, Long businessId) {
        List<AuditRecord> records = auditRecordMapper.selectByBusiness(businessType, businessId);
        return records.stream()
                .map(r -> BeanUtil.copyProperties(r, AuditRecordVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public IPage<AuditRecord> getAuditPage(int pageNum, int pageSize, AuditQueryDTO queryDTO) {
        Page<AuditRecord> page = new Page<>(pageNum, pageSize);
        return auditRecordMapper.selectAuditPage(page,
                queryDTO.getBusinessType(),
                queryDTO.getBusinessCode(),
                queryDTO.getAuditLevel(),
                queryDTO.getAuditStatus(),
                queryDTO.getAuditorId(),
                queryDTO.getStartDate(),
                queryDTO.getEndDate());
    }

    @Override
    public List<AuditRecordVO> getMyPendingAudits(Long auditorId) {
        List<AuditRecord> records = auditRecordMapper.selectPendingByAuditorId(auditorId);
        return records.stream()
                .map(r -> BeanUtil.copyProperties(r, AuditRecordVO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startProcess(Long taskId, Long submitterId, String submitterName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("taskId", taskId);
        variables.put("submitterId", submitterId);
        variables.put("submitterName", submitterName);

        DetectTask task = detectTaskMapper.selectById(taskId);
        if (task != null) {
            variables.put("businessCode", task.getTaskCode());
            variables.put("sampleCode", task.getSampleCode());
        }

        variables.put("firstAuditorId", submitterId);
        variables.put("secondAuditorId", submitterId);

        org.flowable.engine.runtime.ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("detectAuditProcess", String.valueOf(taskId), variables);

        if (task != null) {
            task.setProcessInstanceId(processInstance.getId());
            detectTaskMapper.updateById(task);
        }

        return processInstance.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, String result, String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.AUDIT_NOT_FOUND, "审核任务不存在");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("auditResult", result);
        variables.put("auditComment", comment);

        taskService.complete(taskId, variables);
    }

    @Override
    public List<Map<String, Object>> getMyAuditTasks(Long userId) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(String.valueOf(userId))
                .orderByTaskCreateTime()
                .desc()
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("taskId", task.getId());
            taskMap.put("taskName", task.getName());
            taskMap.put("taskDefinitionKey", task.getTaskDefinitionKey());
            taskMap.put("processInstanceId", task.getProcessInstanceId());
            taskMap.put("createTime", task.getCreateTime());
            taskMap.put("assignee", task.getAssignee());
            taskMap.put("description", task.getDescription());

            Map<String, Object> variables = taskService.getVariables(task.getId());
            taskMap.put("variables", variables);

            result.add(taskMap);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getProcessHistory(String processInstanceId) {
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance == null) {
            throw new BusinessException(ResultCode.AUDIT_NOT_FOUND, "流程实例不存在");
        }

        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (HistoricActivityInstance activity : activities) {
            Map<String, Object> activityMap = new HashMap<>();
            activityMap.put("activityId", activity.getActivityId());
            activityMap.put("activityName", activity.getActivityName());
            activityMap.put("activityType", activity.getActivityType());
            activityMap.put("assignee", activity.getAssignee());
            activityMap.put("startTime", activity.getStartTime());
            activityMap.put("endTime", activity.getEndTime());
            activityMap.put("durationInMillis", activity.getDurationInMillis());

            result.add(activityMap);
        }
        return result;
    }

    @Override
    public AuditFlowVO getAuditFlow(String processInstanceId) {
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance == null) {
            throw new BusinessException(ResultCode.AUDIT_NOT_FOUND, "流程实例不存在");
        }

        AuditFlowVO flowVO = new AuditFlowVO();
        flowVO.setProcessInstanceId(processInstanceId);
        flowVO.setBusinessCode(processInstance.getBusinessKey());
        flowVO.setCurrentStatus(processInstance.getEndTime() == null ? "RUNNING" : "COMPLETED");

        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityType("userTask")
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();

        List<AuditFlowVO.AuditNodeVO> nodes = new ArrayList<>();

        AuditFlowVO.AuditNodeVO submitNode = new AuditFlowVO.AuditNodeVO();
        submitNode.setNodeCode("startEvent");
        submitNode.setNodeName("提交审核");
        submitNode.setNodeType("start");
        submitNode.setStatus("completed");
        submitNode.setAuditorName(processInstance.getStartUserId());
        submitNode.setAuditTime(processInstance.getStartTime());
        nodes.add(submitNode);

        for (HistoricActivityInstance activity : activities) {
            AuditFlowVO.AuditNodeVO node = new AuditFlowVO.AuditNodeVO();
            node.setNodeCode(activity.getActivityId());
            node.setNodeName(activity.getActivityName());
            node.setNodeType("audit");
            node.setAuditorId(activity.getAssignee() != null ? Long.parseLong(activity.getAssignee()) : null);
            node.setAuditorName(activity.getAssignee());
            node.setAuditTime(activity.getStartTime());

            if (activity.getEndTime() != null) {
                node.setStatus("completed");
            } else {
                node.setStatus("pending");
            }

            List<HistoricTaskInstance> taskInstances = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .taskDefinitionKey(activity.getActivityId())
                    .list();

            if (!taskInstances.isEmpty()) {
                HistoricTaskInstance taskInstance = taskInstances.get(0);
                if (StrUtil.isNotBlank(taskInstance.getDeleteReason())) {
                    node.setActionType(taskInstance.getDeleteReason());
                }
            }

            nodes.add(node);
        }

        if (processInstance.getEndTime() != null) {
            AuditFlowVO.AuditNodeVO endNode = new AuditFlowVO.AuditNodeVO();
            endNode.setNodeCode("endEvent");
            endNode.setNodeType("end");
            endNode.setAuditTime(processInstance.getEndTime());

            List<AuditRecord> auditRecords = auditRecordMapper.selectByBusiness("task",
                    Long.parseLong(processInstance.getBusinessKey() != null ? processInstance.getBusinessKey() : "0"));

            boolean allPass = auditRecords.stream()
                    .allMatch(r -> AuditConstants.AUDIT_STATUS_PASS.equals(r.getAuditStatus()));
            boolean hasReject = auditRecords.stream()
                    .anyMatch(r -> AuditConstants.AUDIT_STATUS_REJECT.equals(r.getAuditStatus()));
            boolean hasRetest = auditRecords.stream()
                    .anyMatch(r -> AuditConstants.AUDIT_STATUS_RETEST.equals(r.getAuditStatus()));

            if (allPass) {
                endNode.setNodeName("审核通过");
                endNode.setStatus("approved");
            } else if (hasReject) {
                endNode.setNodeName("审核驳回");
                endNode.setStatus("rejected");
            } else if (hasRetest) {
                endNode.setNodeName("发起复测");
                endNode.setStatus("retest");
            } else {
                endNode.setNodeName("流程结束");
                endNode.setStatus("completed");
            }
            nodes.add(endNode);
        }

        flowVO.setNodes(nodes);
        return flowVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectToSubmitter(String flowTaskId, String reason, Long userId, String userName) {
        Task task = taskService.createTaskQuery().taskId(flowTaskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.AUDIT_NOT_FOUND, "审核任务不存在");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("auditResult", "REJECT");
        variables.put("auditComment", reason);
        variables.put("auditorId", userId);
        variables.put("auditorName", userName);
        variables.put("rejectReason", reason);

        taskService.complete(flowTaskId, variables);

        Long taskId = (Long) taskService.getVariable(flowTaskId, "taskId");
        if (taskId != null) {
            DetectTask detectTask = detectTaskMapper.selectById(taskId);
            if (detectTask != null) {
                detectTask.setTaskStatus(TaskConstants.TASK_STATUS_SUBMITTED);
                detectTaskMapper.updateById(detectTask);
            }
        }

        log.info("审核驳回至检测人员，流程任务ID：{}，驳回原因：{}", flowTaskId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void triggerRetest(String flowTaskId, Long retesterId, String reason, Long userId, String userName) {
        Task task = taskService.createTaskQuery().taskId(flowTaskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.AUDIT_NOT_FOUND, "审核任务不存在");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("auditResult", "RETEST");
        variables.put("auditComment", reason);
        variables.put("auditorId", userId);
        variables.put("auditorName", userName);
        variables.put("retesterId", retesterId);
        variables.put("retestReason", reason);

        taskService.complete(flowTaskId, variables);

        Long taskId = (Long) taskService.getVariable(flowTaskId, "taskId");
        if (taskId != null) {
            DetectTask detectTask = detectTaskMapper.selectById(taskId);
            if (detectTask != null) {
                detectTask.setTaskStatus(TaskConstants.TASK_STATUS_RETEST);
                detectTaskMapper.updateById(detectTask);
            }
        }

        log.info("发起复测，流程任务ID：{}，复测人ID：{}，原因：{}", flowTaskId, retesterId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SamplingReview createSamplingReview(SamplingReviewDTO dto, Long userId, String userName) {
        SamplingReview review = new SamplingReview();
        review.setSampleRate(dto.getSampleRate());
        review.setReviewType(dto.getReviewType());
        review.setReviewStatus(AuditConstants.SAMPLING_REVIEW_STATUS_PENDING);
        review.setReviewerId(userId);
        review.setReviewerName(userName);
        review.setRemark(dto.getRemark());
        review.setCreateBy(userId);
        review.setUpdateBy(userId);
        samplingReviewMapper.insert(review);

        log.info("创建随机抽样复审，抽样率：{}，类型：{}", dto.getSampleRate(), dto.getReviewType());
        return review;
    }

    @Override
    public List<SamplingReview> getPendingSamplingReviews() {
        return samplingReviewMapper.selectPendingReviews();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeSamplingReview(Long reviewId, String result, String opinion, Long userId, String userName) {
        SamplingReview review = samplingReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(ResultCode.SAMPLING_REVIEW_NOT_FOUND);
        }

        review.setReviewStatus(result);
        review.setReviewOpinion(opinion);
        review.setReviewerId(userId);
        review.setReviewerName(userName);
        review.setReviewTime(LocalDateTime.now());
        review.setUpdateBy(userId);
        samplingReviewMapper.updateById(review);

        if (AuditConstants.SAMPLING_REVIEW_STATUS_REJECT.equals(result) && review.getTaskId() != null) {
            DetectTask task = detectTaskMapper.selectById(review.getTaskId());
            if (task != null) {
                task.setTaskStatus(TaskConstants.TASK_STATUS_REJECTED);
                detectTaskMapper.updateById(task);
            }
        }

        log.info("完成抽样复审，复审ID：{}，结果：{}", reviewId, result);
    }

    private void createSecondLevelAudit(AuditSubmitDTO dto, Long previousAuditId, Long userId) {
        AuditRecord secondAudit = new AuditRecord();
        secondAudit.setAuditCode(CodeGenerator.generateAuditCode());
        secondAudit.setBusinessType(dto.getBusinessType());
        secondAudit.setBusinessId(dto.getBusinessId());
        secondAudit.setBusinessCode(dto.getBusinessCode());
        secondAudit.setAuditLevel(2);
        secondAudit.setAuditStatus(AuditConstants.AUDIT_STATUS_PENDING);
        secondAudit.setPreviousAuditId(previousAuditId);
        secondAudit.setCreateBy(userId);
        secondAudit.setUpdateBy(userId);
        save(secondAudit);

        AuditRecord firstAudit = getById(previousAuditId);
        if (firstAudit != null) {
            firstAudit.setNextAuditId(secondAudit.getId());
            updateById(firstAudit);
        }
    }

    private void updateBusinessStatus(String businessType, Long businessId, String status) {
        if ("task".equals(businessType)) {
            DetectTask task = detectTaskMapper.selectById(businessId);
            if (task != null) {
                task.setTaskStatus(status);
                detectTaskMapper.updateById(task);
            }
        }
    }

    private void updateDetectResultsAuditStatus(Long taskId, String isAudit) {
        List<DetectResult> results = detectResultMapper.selectByTaskId(taskId);
        for (DetectResult result : results) {
            result.setIsAudit(isAudit);
            detectResultMapper.updateById(result);
        }
    }
}
