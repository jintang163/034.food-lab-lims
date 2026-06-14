package com.foodlab.audit.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditRecordVO {

    private Long id;

    private String auditCode;

    private String businessType;

    private Long businessId;

    private String businessCode;

    private Integer auditLevel;

    private String auditStatus;

    private Long auditorId;

    private String auditorName;

    private LocalDateTime auditTime;

    private String auditOpinion;

    private Long previousAuditId;

    private Long nextAuditId;

    private Long retestId;

    private String processInstanceId;

    private String actionType;

    private String remark;

    private LocalDateTime createTime;
}
