package com.foodlab.audit.listener;

import com.foodlab.audit.entity.RetestRecord;
import com.foodlab.audit.service.RetestService;
import com.foodlab.common.constant.AuditConstants;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(DelegateExecution execution) {
        Long taskId = (Long) execution.getVariable("taskId");
        Long originalResultId = (Long) execution.getVariable("originalResultId");
        Long triggerAuditId = (Long) execution.getVariable("triggerAuditId");
        String triggerReason = (String) execution.getVariable("triggerReason");
        Long retesterId = (Long) execution.getVariable("retesterId");

        log.info("创建复测记录委托执行，任务ID：{}，原结果ID：{}", taskId, originalResultId);

        execution.setVariable("retestId", null);
    }
}
