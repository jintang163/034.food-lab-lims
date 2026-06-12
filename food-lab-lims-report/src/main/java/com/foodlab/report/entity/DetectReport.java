package com.foodlab.report.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("detect_report")
public class DetectReport extends BaseEntity {

    private String reportCode;

    private String reportName;

    private String reportType;

    private Long templateId;

    private Long sampleId;

    private String sampleCode;

    private Long taskId;

    private String reportContent;

    private String reportFilePath;

    private String reportStatus;

    private LocalDate issueDate;

    private LocalDate expireDate;

    private Long issuerId;

    private String issuerName;

    private String sealStatus;

    private String sealFilePath;

    private Integer downloadCount;

    private LocalDateTime createTime;
}
