package com.foodlab.ncr.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NcrCauseAnalysisVO {

    private Long id;

    private Long ncrId;

    private String ncrCode;

    private String causeType;

    private String causeDescription;

    private String rootCause;

    private String impactAnalysis;

    private Long analysisPersonId;

    private String analysisPersonName;

    private LocalDateTime analysisTime;

    private String reviewPersonId;

    private String reviewPersonName;

    private LocalDateTime reviewTime;

    private String reviewOpinion;

    private String remark;

    private String attachFiles;

    private LocalDateTime createTime;
}
