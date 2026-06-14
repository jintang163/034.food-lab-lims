package com.foodlab.audit.listener;

import com.foodlab.audit.entity.RetestRecord;
import com.foodlab.audit.service.RetestService;
import com.foodlab.common.constant.AuditConstants;
import com.foodlab.common.constant.DetectConstants;
import com.foodlab.detect.entity.DetectResult;
import com.foodlab.detect.mapper.DetectResultMapper;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.service.DetectTaskService;
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
public class RetestAdoptCompleteListener implements TaskListener {

    private final RetestService retestService;
    private final DetectResultMapper detectResultMapper;
    private final DetectTaskService detectTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        Long retestId = (Long) delegateTask.getVariable("retestId");
        String adoptedResult = (String) delegateTask.getVariable("adoptedResult");
        String adoptOpinion = (String) delegateTask.getVariable("adoptOpinion");
        Long triggerAuditorId = (Long) delegateTask.getVariable("triggerAuditorId");
        String triggerAuditorName = (String) delegateTask.getVariable("triggerAuditorName");
        String retestFromNode = (String) delegateTask.getVariable("retestFromNode");

        log.info("对比采用任务完成，流程实例ID：{}，复测ID：{}，采用结果：{}，来源节点：{}",
                processInstanceId, retestId, adoptedResult, retestFromNode);

        if (retestId != null) {
            RetestRecord retestRecord = retestService.getById(retestId);
            if (retestRecord != null) {
                retestRecord.setAdoptedResult(adoptedResult);
                retestRecord.setAdoptOpinion(adoptOpinion);
                retestRecord.setRetestStatus(AuditConstants.RETEST_STATUS_ADOPTED);
                retestRecord.setAdopterId(triggerAuditorId);
                retestRecord.setAdopterName(triggerAuditorName);
                retestRecord.setAdoptTime(LocalDateTime.now());
                retestRecord.setUpdateBy(triggerAuditorId);
                retestService.updateById(retestRecord);

                if (AuditConstants.ADOPTED_RETEST.equals(adoptedResult) && retestRecord.getOriginalResultId() != null) {
                    DetectResult originalResult = detectResultMapper.selectById(retestRecord.getOriginalResultId());
                    if (originalResult != null) {
                        originalResult.setResultValue(retestRecord.getRetestValue());
                        originalResult.setJudgeResult(retestRecord.getRetestJudge());
                        originalResult.setIsAudit(DetectConstants.IS_AUDIT_NO);
                        detectResultMapper.updateById(originalResult);
                        log.info("已更新原检测结果ID：{}，新值：{}", retestRecord.getOriginalResultId(), retestRecord.getRetestValue());
                    }
                }

                if (retestRecord.getTaskId() != null) {
                    DetectTask task = detectTaskService.getById(retestRecord.getTaskId());
                    if (task != null) {
                        task.setTaskStatus(retestFromNode.equals("FIRST") ? "FIRST_AUDIT" : "SECOND_AUDIT");
                        detectTaskService.updateById(task);
                    }
                }

                delegateTask.setVariable("adoptedResult", adoptedResult);
                delegateTask.setVariable("retestId", retestId);
            }
        }

        log.info("复测子流程完成，即将返回主流程节点：{}", retestFromNode);
    }
}
