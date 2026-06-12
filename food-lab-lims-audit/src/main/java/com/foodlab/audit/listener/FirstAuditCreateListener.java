package com.foodlab.audit.listener;

import com.foodlab.audit.entity.AuditRecord;
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
public class FirstAuditCreateListener implements TaskListener {

    private final AuditService auditService;
    private final DetectTaskService detectTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        Long taskId = (Long) delegateTask.getVariable("taskId");
        String businessCode = (String) delegateTask.getVariable("sampleCode");
        Long submitterId = (Long) delegateTask.getVariable("submitterId");

        log.info("一级审核任务创建，流程实例ID：{}，任务ID：{}", processInstanceId, taskId);

        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setAuditCode(CodeGenerator.generateAuditCode());
        auditRecord.setBusinessType("task");
        auditRecord.setBusinessId(taskId);
        auditRecord.setBusinessCode(businessCode);
        auditRecord.setAuditLevel(1);
        auditRecord.setAuditStatus(AuditConstants.AUDIT_STATUS_PENDING);
        auditRecord.setCreateBy(submitterId);
        auditRecord.setUpdateBy(submitterId);
        auditService.save(auditRecord);

        if (taskId != null) {
            DetectTask task = detectTaskService.getById(taskId);
            if (task != null) {
                task.setTaskStatus(TaskConstants.TASK_STATUS_FIRST_AUDIT);
                task.setProcessInstanceId(processInstanceId);
                detectTaskService.updateById(task);
            }
        }

        sendNotification(taskId, businessCode);
    }

    private void sendNotification(Long taskId, String businessCode) {
        log.info("发送一级审核通知，任务ID：{}，样品编号：{}", taskId, businessCode);
    }
}
