package com.foodlab.audit.dto;

import lombok.Data;

import java.util.List;

@Data
public class RetestSubmitDTO {

    private Long taskId;

    private Long originalResultId;

    private Long triggerAuditId;

    private String triggerReason;

    private Long retesterId;
}
