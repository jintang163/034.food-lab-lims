package com.foodlab.audit.listener;

import com.foodlab.audit.entity.AuditRecord;
import com.foodlab.audit.mapper.AuditRecordMapper;
import com.foodlab.audit.service.AuditService;
import com.foodlab.common.constant.AuditConstants;
import com.foodlab.common.constant.TaskConstants;
import com.foodlab.common.utils.CodeGenerator;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.service.DetectTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateTask;
import org.flowable.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecondAuditCreateListener implements TaskListener {

    private final AuditService auditService;
    private final AuditRecordMapper auditRecordMapper;
    private final DetectTaskService detectTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        Long taskId = (Long) delegateTask.getVariable("taskId");
        String businessCode = (String) delegateTask.getVariable("sampleCode");
        Long submitterId = (Long) delegateTask.getVariable("submitterId");

        log.info("二级审核任务创建，流程实例ID：{}，任务ID：{}", processInstanceId, taskId);

        AuditRecord firstAuditRecord = auditRecordMapper.selectLatestByBusinessAndLevel("task", taskId, 1);

        AuditRecord secondAuditRecord = new AuditRecord();
        secondAuditRecord.setAuditCode(CodeGenerator.generateAuditCode());
        secondAuditRecord.setBusinessType("task");
        secondAuditRecord.setBusinessId(taskId);
        secondAuditRecord.setBusinessCode(businessCode);
        secondAuditRecord.setAuditLevel(2);
        secondAuditRecord.setAuditStatus(AuditConstants.AUDIT_STATUS_PENDING);
        secondAuditRecord.setPreviousAuditId(firstAuditRecord != null ? firstAuditRecord.getId() : null);
        secondAuditRecord.setCreateBy(submitterId);
        secondAuditRecord.setUpdateBy(submitterId);
        auditService.save(secondAuditRecord);

        if (firstAuditRecord != null) {
            firstAuditRecord.setNextAuditId(secondAuditRecord.getId());
            auditRecordMapper.updateById(firstAuditRecord);
        }

        if (taskId != null) {
            DetectTask task = detectTaskService.getById(taskId);
            if (task != null) {
                task.setTaskStatus(TaskConstants.TASK_STATUS_SECOND_AUDIT);
                detectTaskService.updateById(task);
            }
        }
    }
}
