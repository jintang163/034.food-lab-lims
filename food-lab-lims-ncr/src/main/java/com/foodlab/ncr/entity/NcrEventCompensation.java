package com.foodlab.ncr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ncr_event_compensation")
public class NcrEventCompensation extends BaseEntity {

    private String eventType;

    private String bizKey;

    private Long detectResultId;

    private Long sampleId;

    private String sampleCode;

    private Long detectItemId;

    private String detectItemName;

    private Long taskId;

    private Long submitterId;

    private String eventPayload;

    private Integer retryCount;

    private Integer maxRetry;

    private String compensationStatus;

    private String lastErrorMsg;

    private LocalDateTime lastRetryTime;

    private LocalDateTime nextRetryTime;
}
