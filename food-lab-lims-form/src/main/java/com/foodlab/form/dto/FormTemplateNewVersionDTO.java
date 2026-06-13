package com.foodlab.form.dto;

import lombok.Data;

@Data
public class FormTemplateNewVersionDTO {

    private String formSchema;

    private String changeSummary;
}
