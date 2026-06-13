package com.foodlab.form.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FormTemplateVersionVO {

    private Long id;

    private Long templateId;

    private String templateCode;

    private String templateName;

    private Long detectItemId;

    private String formSchema;

    private Integer version;

    private String changeType;

    private String changeSummary;

    private String changeDetail;

    private LocalDateTime publishTime;

    private Long publishedBy;

    private Long previousVersionId;

    private Long nextVersionId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
