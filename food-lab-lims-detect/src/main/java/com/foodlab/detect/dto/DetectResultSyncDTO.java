package com.foodlab.detect.dto;

import com.foodlab.common.domain.OfflineSyncData;
import lombok.Data;

import java.util.List;

@Data
public class DetectResultSyncDTO implements OfflineSyncData {

    private Long userId;

    private String deviceId;

    private String syncBatchNo;

    private List<DetectResultSubmitDTO> dataList;
}
