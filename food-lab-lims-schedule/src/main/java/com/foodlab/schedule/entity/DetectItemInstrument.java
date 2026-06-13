package com.foodlab.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("detect_item_instrument")
public class DetectItemInstrument extends BaseEntity {

    private Long detectItemId;
    private Long instrumentId;
    private Integer estimatedDurationMinutes;
    private Integer sortOrder;
    private Integer isRequired;
}
