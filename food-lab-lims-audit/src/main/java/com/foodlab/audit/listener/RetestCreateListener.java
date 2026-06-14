package com.foodlab.audit.listener;

import com.foodlab.audit.entity.RetestRecord;
import com.foodlab.audit.service.RetestService;
import com.foodlab.common.constant.AuditConstants;
import com.foodlab.common.constant.TaskConstants;
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
public class RetestCreateListener implements TaskListener {

    private final RetestService retestService;
    private final DetectTaskService detectTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        Long retestId = (Long) delegateTask.getVariable("retestId");
        Long taskId = (Long) delegateTask.getVariable("taskId");

        log.info("复测检测任务创建，流程实例ID：{}，复测ID：{}", processInstanceId, retestId);

        if (retestId != null) {
            RetestRecord retestRecord = retestService.getById(retestId);
            if (retestRecord != null) {
                retestRecord.setRetestStatus(AuditConstants.RETEST_STATUS_DETECTING);
                retestRecord.setProcessInstanceId(processInstanceId);
                retestService.updateById(retestRecord);
            }
        }

        if (taskId != null) {
            DetectTask task = detectTaskService.getById(taskId);
            if (task != null) {
                task.setTaskStatus(TaskConstants.TASK_STATUS_RETEST);
                detectTaskService.updateById(task);
            }
        }
    }
}
