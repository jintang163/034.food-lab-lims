package com.foodlab.audit.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RetestRecordVO {

    private Long id;

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

    private LocalDateTime createTime;
}
