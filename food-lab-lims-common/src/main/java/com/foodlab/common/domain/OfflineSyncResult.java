package com.foodlab.common.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OfflineSyncResult implements Serializable {

    private Boolean success;
    private String message;
    private Integer successCount;
    private Integer failCount;
    private List<Long> successIds;
    private List<SyncFailItem> failItems;

    @Data
    public static class SyncFailItem implements Serializable {
        private Long id;
        private String reason;
    }
}
