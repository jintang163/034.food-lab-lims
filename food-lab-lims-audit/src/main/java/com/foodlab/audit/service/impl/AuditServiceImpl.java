package com.foodlab.audit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodlab.audit.dto.AuditQueryDTO;
import com.foodlab.audit.dto.AuditSubmitDTO;
import com.foodlab.audit.entity.AuditRecord;
import com.foodlab.audit.mapper.AuditRecordMapper;
import com.foodlab.audit.service.AuditService;
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

        currentRecord.setAuditStatus(isPass ? AuditConstants.AUDIT_STATUS_PASS : AuditConstants.AUDIT_STATUS_REJECT);
        currentRecord.setAuditorId(userId);
        currentRecord.setAuditorName(userName);
        currentRecord.setAuditTime(LocalDateTime.now());
        currentRecord.setAuditOpinion(dto.getAuditOpinion());
        currentRecord.setRemark(dto.getRemark());
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
        return BeanUtil.copyProperties(record, AuditRecordVO.class);
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

        runtimeService.startProcessInstanceByKey("detectAuditProcess", String.valueOf(taskId), variables);

        return String.valueOf(taskId);
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
