package com.foodlab.audit.listener;

import com.foodlab.audit.entity.RetestRecord;
import com.foodlab.audit.service.RetestService;
import com.foodlab.common.constant.AuditConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateTask;
import org.flowable.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetestAdoptCreateListener implements TaskListener {

    private final RetestService retestService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        Long retestId = (Long) delegateTask.getVariable("retestId");
        String retestCode = (String) delegateTask.getVariable("retestCode");
        Long triggerAuditorId = (Long) delegateTask.getVariable("triggerAuditorId");
        String triggerAuditorName = (String) delegateTask.getVariable("triggerAuditorName");
        String retestFromNode = (String) delegateTask.getVariable("retestFromNode");

        log.info("对比采用任务创建，流程实例ID：{}，复测ID：{}，审核人：{}，来源节点：{}",
                processInstanceId, retestId, triggerAuditorName, retestFromNode);

        delegateTask.setAssignee(String.valueOf(triggerAuditorId));
        delegateTask.setOwner(triggerAuditorName);

        if (retestId != null) {
            RetestRecord retestRecord = retestService.getById(retestId);
            if (retestRecord != null) {
                retestRecord.setRetestStatus(AuditConstants.RETEST_STATUS_COMPLETED);
                retestService.updateById(retestRecord);

                delegateTask.setVariable("originalValue", retestRecord.getOriginalValue());
                delegateTask.setVariable("originalJudge", retestRecord.getOriginalJudge());
                delegateTask.setVariable("retestValue", retestRecord.getRetestValue());
                delegateTask.setVariable("retestJudge", retestRecord.getRetestJudge());
                delegateTask.setVariable("triggerReason", retestRecord.getTriggerReason());
            }
        }
    }
}
