package com.foodlab.audit.listener;

import com.foodlab.audit.service.AuditService;
import com.foodlab.common.event.TaskCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCompletedEventListener {

    private final AuditService auditService;

    @Async
    @EventListener
    public void handleTaskCompleted(TaskCompletedEvent event) {
        log.info("收到任务完成事件，开始启动审核流程，任务ID：{}，任务编号：{}",
                event.getTaskId(), event.getTaskCode());

        try {
            String processInstanceId = auditService.startProcess(
                    event.getTaskId(),
                    event.getSubmitterId(),
                    event.getSubmitterName()
            );
            log.info("审核流程启动成功，任务ID：{}，流程实例ID：{}", event.getTaskId(), processInstanceId);
        } catch (Exception e) {
            log.error("启动审核流程失败，任务ID：{}", event.getTaskId(), e);
        }
    }
}
