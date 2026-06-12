package com.foodlab.audit.dto;

import lombok.Data;

@Data
public class AuditQueryDTO {

    private String businessType;

    private String businessCode;

    private Integer auditLevel;

    private String auditStatus;

    private Long auditorId;

    private String startDate;

    private String endDate;
}
