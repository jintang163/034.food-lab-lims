package com.foodlab.form.dto;

import lombok.Data;

@Data
public class FormDataQueryDTO {

    private Long templateId;

    private Long sampleId;

    private Long taskId;

    private String status;
}
