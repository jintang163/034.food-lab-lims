package com.foodlab.detect.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("detect_item")
public class DetectItem extends BaseEntity {

    private String itemCode;

    private String itemName;

    private Long categoryId;

    private String detectMethod;

    private String detectStandard;

    private String unit;

    private BigDecimal precisionValue;

    private String formSchema;

    private String status;

    @TableField(exist = false)
    private String categoryName;
}
