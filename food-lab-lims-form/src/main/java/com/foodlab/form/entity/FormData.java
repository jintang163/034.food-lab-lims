package com.foodlab.form.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("form_data")
public class FormData extends BaseEntity {

    private String dataCode;

    private Long templateId;

    private String templateCode;

    private Integer templateVersion;

    private Long detectItemId;

    private Long sampleId;

    private String sampleCode;

    private Long taskId;

    private String formData;

    private LocalDateTime submitTime;

    private Long submittedBy;

    private String submittedByName;

    private String status;

    private String syncStatus;

    private String offlineId;

    private String deviceId;

    private String remark;
}
