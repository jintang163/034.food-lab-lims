package com.foodlab.common.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OfflineSyncData<T> implements Serializable {

    private String deviceId;
    private Long userId;
    private Long syncTime;
    private String syncBatchNo;
    private List<T> dataList;
    private Integer totalCount;
}
