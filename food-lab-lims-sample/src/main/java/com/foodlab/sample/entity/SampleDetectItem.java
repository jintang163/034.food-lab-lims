package com.foodlab.sample.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sample_detect_item")
public class SampleDetectItem extends BaseEntity {

    private Long sampleId;

    private String sampleCode;

    private Long detectItemId;

    private String detectItemName;

    private Long limitStandardId;

    private Integer sort;
}
