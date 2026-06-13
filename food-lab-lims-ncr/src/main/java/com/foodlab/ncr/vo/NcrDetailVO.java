package com.foodlab.ncr.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NcrDetailVO {

    private Long id;

    private String ncrCode;

    private String ncrSource;

    private Long sampleId;

    private String sampleCode;

    private String sampleName;

    private Long detectResultId;

    private Long detectItemId;

    private String detectItemName;

    private String unqualifiedDescription;

    private String ncrStatus;

    private String currentStage;

    private LocalDateTime recheckTime;

    private LocalDateTime causeAnalysisTime;

    private LocalDateTime correctiveActionTime;

    private LocalDateTime preventiveActionTime;

    private LocalDateTime closeTime;

    private Long closePersonId;

    private String closePersonName;

    private String closeRemark;

    private String remark;

    private String attachFiles;

    private LocalDateTime createTime;

    private List<NcrRecheckVO> recheckList;

    private NcrCauseAnalysisVO causeAnalysis;

    private List<NcrCorrectiveActionVO> correctiveActionList;

    private List<NcrPreventiveActionVO> preventiveActionList;
}
