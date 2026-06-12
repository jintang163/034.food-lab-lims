package com.foodlab.detect.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("limit_standard")
public class LimitStandard extends BaseEntity {

    private String standardName;

    private String standardNo;

    private Long detectItemId;

    private String limitType;

    private BigDecimal limitValueMin;

    private BigDecimal limitValueMax;

    private String limitUnit;

    private String qualitativeResult;

    private String description;

    private String status;
}
