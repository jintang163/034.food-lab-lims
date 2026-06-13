package com.foodlab.ncr.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NcrCauseAnalysisSubmitDTO {

    private Long ncrId;

    private String causeType;

    private String causeDescription;

    private String rootCause;

    private String impactAnalysis;

    private Long analysisPersonId;

    private String analysisPersonName;

    private LocalDateTime analysisTime;

    private String remark;

    private String attachFiles;
}
