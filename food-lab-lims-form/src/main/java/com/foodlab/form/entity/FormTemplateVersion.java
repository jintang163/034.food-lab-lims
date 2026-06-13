package com.foodlab.form.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("form_template_version")
public class FormTemplateVersion extends BaseEntity {

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
}
