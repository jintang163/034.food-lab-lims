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

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetestCompleteListener implements TaskListener {

    private final RetestService retestService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        Long retestId = (Long) delegateTask.getVariable("retestId");
        String retestValue = (String) delegateTask.getVariable("retestValue");
        String retestJudge = (String) delegateTask.getVariable("retestJudge");
        Long retesterId = (Long) delegateTask.getVariable("retesterId");
        String retesterName = (String) delegateTask.getVariable("retesterName");

        log.info("复测检测任务完成，流程实例ID：{}，复测ID：{}", processInstanceId, retestId);

        if (retestId != null) {
            RetestRecord retestRecord = retestService.getById(retestId);
            if (retestRecord != null) {
                retestRecord.setRetestValue(retestValue);
                retestRecord.setRetestJudge(retestJudge);
                retestRecord.setRetestStatus(AuditConstants.RETEST_STATUS_COMPLETED);
                retestRecord.setRetesterId(retesterId);
                retestRecord.setRetesterName(retesterName);
                retestRecord.setRetestTime(LocalDateTime.now());
                retestRecord.setUpdateBy(retesterId);
                retestService.updateById(retestRecord);
            }
        }
    }
}
