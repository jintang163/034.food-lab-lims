package com.foodlab.form.dto;

import lombok.Data;

@Data
public class FormTemplateSaveDTO {

    private String templateName;

    private Long detectItemId;

    private String detectItemName;

    private String description;

    private String formSchema;

    private String remark;
}
