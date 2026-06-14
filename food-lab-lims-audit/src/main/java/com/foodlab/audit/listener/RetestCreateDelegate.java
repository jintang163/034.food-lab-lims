package com.foodlab.audit.listener;

import com.foodlab.audit.entity.RetestRecord;
import com.foodlab.audit.service.RetestService;
import com.foodlab.common.constant.AuditConstants;
import com.foodlab.common.utils.CodeGenerator;
import com.foodlab.detect.entity.DetectResult;
import com.foodlab.detect.mapper.DetectResultMapper;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.service.DetectTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("retestCreateDelegate")
@RequiredArgsConstructor
public class RetestCreateDelegate implements JavaDelegate {

    private final RetestService retestService;
    private final DetectResultMapper detectResultMapper;
    private final DetectTaskService detectTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(DelegateExecution execution) {
        Long taskId = (Long) execution.getVariable("taskId");
        Long originalResultId = (Long) execution.getVariable("originalResultId");
        Long triggerAuditId = (Long) execution.getVariable("triggerAuditId");
        String triggerReason = (String) execution.getVariable("triggerReason");
        Long retesterId = (Long) execution.getVariable("retesterId");
        String sampleCode = (String) execution.getVariable("sampleCode");
        String detectItemName = (String) execution.getVariable("detectItemName");
        String retestFromNode = (String) execution.getVariable("retestFromNode");
        Long triggerAuditorId = (Long) execution.getVariable("triggerAuditorId");
        String triggerAuditorName = (String) execution.getVariable("triggerAuditorName");

        log.info("创建复测记录，任务ID：{}，原结果ID：{}，来源节点：{}", taskId, originalResultId, retestFromNode);

        DetectResult originalResult = null;
        if (originalResultId != null) {
            originalResult = detectResultMapper.selectById(originalResultId);
        }

        DetectTask task = null;
        if (taskId != null) {
            task = detectTaskService.getById(taskId);
        }

        RetestRecord retestRecord = new RetestRecord();
        retestRecord.setRetestCode(CodeGenerator.generateRetestCode());
        retestRecord.setOriginalResultId(originalResultId);
        retestRecord.setTaskId(taskId);
        retestRecord.setSampleCode(task != null ? task.getSampleCode() : sampleCode);
        retestRecord.setDetectItemName(originalResult != null ? originalResult.getDetectItemName() : detectItemName);
        retestRecord.setTriggerAuditId(triggerAuditId);
        retestRecord.setTriggerReason(triggerReason);
        retestRecord.setOriginalValue(originalResult != null ? originalResult.getResultValue() : null);
        retestRecord.setOriginalJudge(originalResult != null ? originalResult.getJudgeResult() : null);
        retestRecord.setRetestStatus(AuditConstants.RETEST_STATUS_PENDING);
        retestRecord.setRetesterId(retesterId);
        retestRecord.setProcessInstanceId(execution.getProcessInstanceId());
        retestRecord.setCreateBy(triggerAuditorId);
        retestRecord.setUpdateBy(triggerAuditorId);
        retestService.save(retestRecord);

        execution.setVariable("retestId", retestRecord.getId());
        execution.setVariable("retestCode", retestRecord.getRetestCode());

        if (task != null) {
            task.setTaskStatus("RETEST");
            detectTaskService.updateById(task);
        }

        log.info("复测记录已创建，复测编号：{}，流程实例ID：{}", retestRecord.getRetestCode(), execution.getProcessInstanceId());
    }
}
