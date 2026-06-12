package com.foodlab.audit.listener;

import com.foodlab.common.constant.DetectConstants;
import com.foodlab.common.constant.TaskConstants;
import com.foodlab.detect.entity.DetectResult;
import com.foodlab.detect.mapper.DetectResultMapper;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.service.DetectTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovedEndListener implements ExecutionListener {

    private final DetectTaskService detectTaskService;
    private final DetectResultMapper detectResultMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Long taskId = (Long) execution.getVariable("taskId");

        log.info("审核流程通过结束，流程实例ID：{}，任务ID：{}", processInstanceId, taskId);

        if (taskId != null) {
            DetectTask task = detectTaskService.getById(taskId);
            if (task != null) {
                task.setTaskStatus(TaskConstants.TASK_STATUS_APPROVED);
                detectTaskService.updateById(task);
                log.info("任务状态已更新为APPROVED，任务ID：{}", taskId);
            }

            List<DetectResult> results = detectResultMapper.selectByTaskId(taskId);
            for (DetectResult result : results) {
                result.setIsAudit(DetectConstants.IS_AUDIT_YES);
                detectResultMapper.updateById(result);
            }
            log.info("检测结果已更新为已审核，任务ID：{}", taskId);
        }
    }
}
