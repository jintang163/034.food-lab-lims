package com.foodlab.form.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FormDataVO {

    private Long id;

    private String dataCode;

    private Long templateId;

    private String templateCode;

    private Integer templateVersion;

    private Long detectItemId;

    private Long sampleId;

    private String sampleCode;

    private Long taskId;

    private String formData;

    private Object parsedFormData;

    private LocalDateTime submitTime;

    private Long submittedBy;

    private String submittedByName;

    private String status;

    private String syncStatus;

    private String offlineId;

    private String deviceId;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
