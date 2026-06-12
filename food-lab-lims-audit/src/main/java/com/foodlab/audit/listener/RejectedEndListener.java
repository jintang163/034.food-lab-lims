package com.foodlab.audit.listener;

import com.foodlab.common.constant.TaskConstants;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.service.DetectTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectedEndListener implements ExecutionListener {

    private final DetectTaskService detectTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Long taskId = (Long) execution.getVariable("taskId");

        log.info("审核流程驳回结束，流程实例ID：{}，任务ID：{}", processInstanceId, taskId);

        if (taskId != null) {
            DetectTask task = detectTaskService.getById(taskId);
            if (task != null) {
                task.setTaskStatus(TaskConstants.TASK_STATUS_REJECTED);
                detectTaskService.updateById(task);
                log.info("任务状态已更新为REJECTED，任务ID：{}", taskId);
            }
        }
    }
}
