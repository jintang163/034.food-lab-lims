package com.foodlab.form.dto;

import lombok.Data;

@Data
public class FormTemplateQueryDTO {

    private String templateName;

    private String status;

    private Long detectItemId;
}
