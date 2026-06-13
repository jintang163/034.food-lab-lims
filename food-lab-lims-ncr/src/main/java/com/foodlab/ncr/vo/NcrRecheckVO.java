package com.foodlab.ncr.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class NcrRecheckVO {

    private Long id;

    private Long ncrId;

    private String ncrCode;

    private Integer recheckCount;

    private String recheckStatus;

    private Long taskId;

    private Long sampleId;

    private String sampleCode;

    private Long detectItemId;

    private String detectItemName;

    private String detectMethod;

    private String detectStandard;

    private String instrument;

    private LocalDateTime detectTime;

    private Long detectPersonId;

    private String detectPersonName;

    private String resultType;

    private BigDecimal resultValue;

    private String resultUnit;

    private String qualitativeResult;

    private String limitType;

    private BigDecimal limitValueMin;

    private BigDecimal limitValueMax;

    private String finalJudge;

    private String recheckRemark;

    private String attachFiles;

    private LocalDateTime createTime;
}
