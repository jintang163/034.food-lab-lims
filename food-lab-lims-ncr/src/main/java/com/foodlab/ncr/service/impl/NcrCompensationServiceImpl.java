package com.foodlab.ncr.service.impl;

import com.foodlab.common.constant.NcrConstants;
import com.foodlab.common.event.DetectResultUnqualifiedEvent;
import com.foodlab.common.utils.JsonUtils;
import com.foodlab.ncr.entity.NcrEventCompensation;
import com.foodlab.ncr.mapper.NcrEventCompensationMapper;
import com.foodlab.ncr.service.NcrCompensationService;
import com.foodlab.ncr.service.NcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NcrCompensationServiceImpl implements NcrCompensationService {

    private final NcrEventCompensationMapper compensationMapper;
    private final NcrService ncrService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCompensationRecord(DetectResultUnqualifiedEvent event, String errorMsg) {
        NcrEventCompensation exist = compensationMapper.selectByEventAndBizKey(
                NcrConstants.EVENT_TYPE_DETECT_UNQUALIFIED,
                String.valueOf(event.getDetectResultId())
        );
        if (exist != null) {
            log.warn("补偿记录已存在，检测结果ID：{}", event.getDetectResultId());
            return;
        }

        NcrEventCompensation compensation = new NcrEventCompensation();
        compensation.setEventType(NcrConstants.EVENT_TYPE_DETECT_UNQUALIFIED);
        compensation.setBizKey(String.valueOf(event.getDetectResultId()));
        compensation.setDetectResultId(event.getDetectResultId());
        compensation.setSampleId(event.getSampleId());
        compensation.setSampleCode(event.getSampleCode());
        compensation.setDetectItemId(event.getDetectItemId());
        compensation.setDetectItemName(event.getDetectItemName());
        compensation.setTaskId(event.getTaskId());
        compensation.setSubmitterId(event.getSubmitterId());
        compensation.setEventPayload(JsonUtils.toJson(event));
        compensation.setRetryCount(0);
        compensation.setMaxRetry(5);
        compensation.setCompensationStatus(NcrConstants.COMPENSATION_STATUS_FAILED);
        compensation.setLastErrorMsg(errorMsg);
        compensation.setLastRetryTime(LocalDateTime.now());
        compensation.setNextRetryTime(LocalDateTime.now().plusMinutes(1));
        compensation.setCreateBy(event.getSubmitterId());
        compensation.setUpdateBy(event.getSubmitterId());

        compensationMapper.insert(compensation);
        log.info("已保存NCR补偿记录，检测结果ID：{}，下次重试时间：{}", event.getDetectResultId(), compensation.getNextRetryTime());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCompensationSuccess(Long compensationId) {
        NcrEventCompensation compensation = compensationMapper.selectById(compensationId);
        if (compensation != null) {
            compensation.setCompensationStatus(NcrConstants.COMPENSATION_STATUS_SUCCESS);
            compensation.setLastRetryTime(LocalDateTime.now());
            compensation.setRetryCount(compensation.getRetryCount() + 1);
            compensation.setUpdateBy(compensation.getSubmitterId());
            compensationMapper.updateById(compensation);
            log.info("NCR补偿记录处理成功，补偿ID：{}", compensationId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCompensationFailed(Long compensationId, String errorMsg) {
        NcrEventCompensation compensation = compensationMapper.selectById(compensationId);
        if (compensation != null) {
            compensation.setRetryCount(compensation.getRetryCount() + 1);
            compensation.setLastErrorMsg(errorMsg);
            compensation.setLastRetryTime(LocalDateTime.now());

            if (compensation.getRetryCount() >= compensation.getMaxRetry()) {
                compensation.setCompensationStatus(NcrConstants.COMPENSATION_STATUS_FAILED);
                log.error("NCR补偿记录已达最大重试次数，补偿ID：{}，错误：{}", compensationId, errorMsg);
            } else {
                compensation.setCompensationStatus(NcrConstants.COMPENSATION_STATUS_FAILED);
                int delayMinutes = (int) Math.pow(2, compensation.getRetryCount());
                compensation.setNextRetryTime(LocalDateTime.now().plusMinutes(Math.min(delayMinutes, 60)));
                log.warn("NCR补偿记录处理失败，补偿ID：{}，下次重试时间：{}，错误：{}",
                        compensationId, compensation.getNextRetryTime(), errorMsg);
            }
            compensation.setUpdateBy(compensation.getSubmitterId());
            compensationMapper.updateById(compensation);
        }
    }

    @Override
    @Scheduled(fixedDelay = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void retryPendingCompensations() {
        List<NcrEventCompensation> pendingList = compensationMapper.selectPendingRetry(LocalDateTime.now(), 10);
        if (pendingList.isEmpty()) {
            return;
        }

        log.info("开始处理NCR补偿重试任务，待处理数量：{}", pendingList.size());

        for (NcrEventCompensation compensation : pendingList) {
            try {
                int locked = compensationMapper.lockForProcessing(compensation.getId());
                if (locked == 0) {
                    continue;
                }

                ncrService.autoCreateNcrFromDetectResult(
                        compensation.getDetectResultId(),
                        compensation.getSubmitterId()
                );

                updateCompensationSuccess(compensation.getId());
            } catch (Exception e) {
                log.error("NCR补偿重试失败，补偿ID：{}", compensation.getId(), e);
                try {
                    updateCompensationFailed(compensation.getId(), e.getMessage());
                } catch (Exception ex) {
                    log.error("更新NCR补偿失败状态异常，补偿ID：{}", compensation.getId(), ex);
                }
            }
        }
    }
}
