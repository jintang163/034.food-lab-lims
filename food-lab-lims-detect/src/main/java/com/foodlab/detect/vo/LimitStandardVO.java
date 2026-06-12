package com.foodlab.detect.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LimitStandardVO {

    private Long id;

    private String standardName;

    private String standardNo;

    private String limitType;

    private BigDecimal limitValueMin;

    private BigDecimal limitValueMax;

    private String limitUnit;

    private String qualitativeResult;

    private String description;
}
