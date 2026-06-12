package com.foodlab.detect.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DetectItemVO {

    private Long id;

    private String itemCode;

    private String itemName;

    private Long categoryId;

    private String categoryName;

    private String detectMethod;

    private String detectStandard;

    private String unit;

    private BigDecimal precisionValue;

    private String formSchema;

    private String status;

    private List<LimitStandardVO> limitStandards;
}
