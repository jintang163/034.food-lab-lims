package com.foodlab.audit.listener;

import com.foodlab.audit.service.RetestService;
import com.foodlab.common.constant.AuditConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("retestTriggerDelegate")
@RequiredArgsConstructor
public class RetestTriggerDelegate implements JavaDelegate {

    private final RetestService retestService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Long taskId = (Long) execution.getVariable("taskId");
        Long auditResultId = (Long) execution.getVariable("auditResultId");
        String auditResult = (String) execution.getVariable("auditResult");

        log.info("复测触发委托执行，流程实例ID：{}，任务ID：{}，审核结果：{}", processInstanceId, taskId, auditResult);

        execution.setVariable("retestTriggered", true);
    }
}
