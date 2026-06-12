package com.foodlab.audit.listener;

import com.foodlab.audit.entity.AuditRecord;
import com.foodlab.audit.mapper.AuditRecordMapper;
import com.foodlab.common.constant.AuditConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateTask;
import org.flowable.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirstAuditCompleteListener implements TaskListener {

    private final AuditRecordMapper auditRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        Long taskId = (Long) delegateTask.getVariable("taskId");
        String auditResult = (String) delegateTask.getVariable("auditResult");
        String auditOpinion = (String) delegateTask.getVariable("auditOpinion");
        Long auditorId = (Long) delegateTask.getVariable("auditorId");
        String auditorName = (String) delegateTask.getVariable("auditorName");

        log.info("一级审核任务完成，流程实例ID：{}，任务ID：{}，审核结果：{}", processInstanceId, taskId, auditResult);

        AuditRecord auditRecord = auditRecordMapper.selectLatestByBusinessAndLevel("task", taskId, 1);
        if (auditRecord != null) {
            auditRecord.setAuditStatus(AuditConstants.AUDIT_RESULT_PASS.equals(auditResult)
                    ? AuditConstants.AUDIT_STATUS_PASS
                    : AuditConstants.AUDIT_STATUS_REJECT);
            auditRecord.setAuditorId(auditorId);
            auditRecord.setAuditorName(auditorName);
            auditRecord.setAuditTime(LocalDateTime.now());
            auditRecord.setAuditOpinion(auditOpinion);
            auditRecord.setUpdateBy(auditorId);
            auditRecordMapper.updateById(auditRecord);
        }

        delegateTask.setVariable("auditResult", auditResult);
    }
}
