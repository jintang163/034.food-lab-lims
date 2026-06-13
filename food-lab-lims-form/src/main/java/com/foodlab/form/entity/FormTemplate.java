package com.foodlab.form.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("form_template")
public class FormTemplate extends BaseEntity {

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
}
