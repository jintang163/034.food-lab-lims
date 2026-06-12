package com.foodlab.detect.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("detect_item_category")
public class DetectItemCategory extends BaseEntity {

    private String categoryName;

    private String categoryCode;

    private Long parentId;

    private Integer orderNum;
}
