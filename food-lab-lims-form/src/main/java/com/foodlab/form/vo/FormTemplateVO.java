package com.foodlab.form.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FormTemplateVO {

    private Long id;

    private String templateCode;

    private String templateName;

    private Long detectItemId;

    private String detectItemName;

    private String description;

    private String formSchema;

    private Integer version;

    private Boolean isCurrent;

    private String status;

    private LocalDateTime publishTime;

    private Long publishedBy;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<FormFieldMappingVO> fieldMappings;
}
