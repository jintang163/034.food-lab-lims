package com.foodlab.ncr.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class NcrRecheckSubmitDTO {

    private Long ncrId;

    private Long taskId;

    private String instrument;

    private LocalDateTime detectTime;

    private String resultType;

    private BigDecimal resultValue;

    private String resultUnit;

    private String qualitativeResult;

    private String finalJudge;

    private String recheckRemark;

    private String attachFiles;
}
