package com.foodlab.form.dto;

import lombok.Data;

@Data
public class FormDataSaveDTO {

    private Long templateId;

    private String templateCode;

    private Integer templateVersion;

    private Long detectItemId;

    private Long sampleId;

    private String sampleCode;

    private Long taskId;

    private String formData;

    private String remark;
}
