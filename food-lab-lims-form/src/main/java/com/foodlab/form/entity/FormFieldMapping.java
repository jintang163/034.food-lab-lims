package com.foodlab.form.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("form_field_mapping")
public class FormFieldMapping extends BaseEntity {

    private Long templateId;

    private String templateCode;

    private String fieldKey;

    private String fieldLabel;

    private String fieldType;

    private String businessTable;

    private String businessColumn;

    private Boolean isResultField;

    private Boolean isRequired;

    private Integer sortOrder;
}
