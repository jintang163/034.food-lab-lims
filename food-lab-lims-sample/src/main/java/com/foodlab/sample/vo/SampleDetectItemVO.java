package com.foodlab.sample.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SampleDetectItemVO implements Serializable {

    private Long id;

    private Long detectItemId;

    private String detectItemCode;

    private String detectItemName;

    private String detectMethod;

    private String detectStandard;

    private BigDecimal standardMin;

    private BigDecimal standardMax;

    private String standardUnit;

    private String judgeType;

    private Integer sort;
}
