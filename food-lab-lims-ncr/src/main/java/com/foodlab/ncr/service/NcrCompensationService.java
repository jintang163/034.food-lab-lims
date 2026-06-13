package com.foodlab.ncr.service;

import com.foodlab.common.event.DetectResultUnqualifiedEvent;

public interface NcrCompensationService {

    void saveCompensationRecord(DetectResultUnqualifiedEvent event, String errorMsg);

    void updateCompensationSuccess(Long compensationId);

    void updateCompensationFailed(Long compensationId, String errorMsg);

    void retryPendingCompensations();
}
