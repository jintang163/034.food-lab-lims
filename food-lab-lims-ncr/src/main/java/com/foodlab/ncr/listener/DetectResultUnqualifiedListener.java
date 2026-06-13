package com.foodlab.ncr.listener;

import com.foodlab.common.event.DetectResultUnqualifiedEvent;
import com.foodlab.ncr.service.NcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetectResultUnqualifiedListener {

    private final NcrService ncrService;

    @Async
    @EventListener
    public void handleDetectResultUnqualified(DetectResultUnqualifiedEvent event) {
        log.info("收到检测结果不合格事件，开始启动不合格品处置流程，检测结果ID：{}，样品编号：{}，检测项目：{}",
                event.getDetectResultId(), event.getSampleCode(), event.getDetectItemName());

        try {
            ncrService.autoCreateNcrFromDetectResult(event.getDetectResultId(), event.getSubmitterId());
            log.info("不合格品处置流程启动成功，检测结果ID：{}", event.getDetectResultId());
        } catch (Exception e) {
            log.error("启动不合格品处置流程失败，检测结果ID：{}", event.getDetectResultId(), e);
        }
    }
}
