package com.foodlab.audit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("retest_record")
public class RetestRecord extends BaseEntity {

    private String retestCode;

    private Long originalResultId;

    private Long taskId;

    private String sampleCode;

    private String detectItemName;

    private Long triggerAuditId;

    private String triggerReason;

    private String originalValue;

    private String originalJudge;

    private String retestValue;

    private String retestJudge;

    private String adoptedResult;

    private String retestStatus;

    private Long retesterId;

    private String retesterName;

    private LocalDateTime retestTime;

    private Long adopterId;

    private String adopterName;

    private LocalDateTime adoptTime;

    private String adoptOpinion;

    private String processInstanceId;

    private String remark;
}
