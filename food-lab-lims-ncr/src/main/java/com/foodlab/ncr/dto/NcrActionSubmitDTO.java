package com.foodlab.ncr.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NcrActionSubmitDTO {

    private Long ncrId;

    private String actionDescription;

    private String actionPlan;

    private Long actionPersonId;

    private String actionPersonName;

    private LocalDateTime planStartTime;

    private LocalDateTime planEndTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private String actionStatus;

    private String actionResult;

    private String effectivenessEvaluation;

    private Long verifyPersonId;

    private String verifyPersonName;

    private LocalDateTime verifyTime;

    private String verifyOpinion;

    private String remark;

    private String attachFiles;
}
