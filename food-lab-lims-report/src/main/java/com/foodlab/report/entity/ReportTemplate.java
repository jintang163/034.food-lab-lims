package com.foodlab.report.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report_template")
public class ReportTemplate extends BaseEntity {

    private String templateCode;

    private String templateName;

    private String templateType;

    private String templateContent;

    private String templateFile;

    private String isDefault;

    private String status;
}
