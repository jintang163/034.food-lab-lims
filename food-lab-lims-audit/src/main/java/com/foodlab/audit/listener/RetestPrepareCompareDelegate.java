package com.foodlab.audit.listener;

import com.foodlab.audit.entity.RetestRecord;
import com.foodlab.audit.service.RetestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("retestPrepareCompareDelegate")
@RequiredArgsConstructor
public class RetestPrepareCompareDelegate implements JavaDelegate {

    private final RetestService retestService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(DelegateExecution execution) {
        Long retestId = (Long) execution.getVariable("retestId");
        String retestCode = (String) execution.getVariable("retestCode");
        String retestValue = (String) execution.getVariable("retestValue");
        String retestJudge = (String) execution.getVariable("retestJudge");
        Long retesterId = (Long) execution.getVariable("retesterId");
        String retesterName = (String) execution.getVariable("retesterName");

        log.info("准备复测对比数据，复测ID：{}，复测值：{}", retestId, retestValue);

        if (retestId != null) {
            RetestRecord retestRecord = retestService.getById(retestId);
            if (retestRecord != null) {
                if (retestValue != null) {
                    retestRecord.setRetestValue(retestValue);
                }
                if (retestJudge != null) {
                    retestRecord.setRetestJudge(retestJudge);
                }
                retestService.updateById(retestRecord);

                execution.setVariable("originalValue", retestRecord.getOriginalValue());
                execution.setVariable("originalJudge", retestRecord.getOriginalJudge());
                execution.setVariable("retestValueForCompare", retestRecord.getRetestValue());
                execution.setVariable("retestJudgeForCompare", retestRecord.getRetestJudge());
            }
        }
    }
}
