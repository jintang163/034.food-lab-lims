package com.foodlab.audit.dto;

import lombok.Data;

@Data
public class AuditSubmitDTO {

    private String businessType;

    private Long businessId;

    private String businessCode;

    private Integer auditLevel;

    private String auditResult;

    private String auditOpinion;

    private String remark;
}
