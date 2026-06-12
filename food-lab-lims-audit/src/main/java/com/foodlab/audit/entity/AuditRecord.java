package com.foodlab.audit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("audit_record")
public class AuditRecord extends BaseEntity {

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

    private String remark;
}
