package com.foodlab.form.vo;

import lombok.Data;

@Data
public class FormFieldMappingVO {

    private Long id;

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
