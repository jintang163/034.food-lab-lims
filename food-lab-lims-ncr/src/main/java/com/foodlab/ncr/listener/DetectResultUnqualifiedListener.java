package com.foodlab.ncr.listener;

import com.foodlab.common.event.DetectResultUnqualifiedEvent;
import com.foodlab.ncr.service.NcrCompensationService;
import com.foodlab.ncr.service.NcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetectResultUnqualifiedListener {

    private final NcrService ncrService;
    private final NcrCompensationService ncrCompensationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleDetectResultUnqualified(DetectResultUnqualifiedEvent event) {
        log.info("收到检测结果不合格事件（事务已提交），开始启动不合格品处置流程，检测结果ID：{}，样品编号：{}，检测项目：{}",
                event.getDetectResultId(), event.getSampleCode(), event.getDetectItemName());

        try {
            ncrService.autoCreateNcrFromDetectResult(event.getDetectResultId(), event.getSubmitterId());
            log.info("不合格品处置流程启动成功，检测结果ID：{}", event.getDetectResultId());
        } catch (Exception e) {
            log.error("启动不合格品处置流程失败，检测结果ID：{}，将记录补偿信息进行重试", event.getDetectResultId(), e);
            try {
                ncrCompensationService.saveCompensationRecord(event, e.getMessage());
            } catch (Exception ex) {
                log.error("保存NCR补偿记录失败，检测结果ID：{}", event.getDetectResultId(), ex);
            }
        }
    }
}
