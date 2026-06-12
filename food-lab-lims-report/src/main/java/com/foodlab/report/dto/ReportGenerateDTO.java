package com.foodlab.report.dto;

import lombok.Data;

@Data
public class ReportGenerateDTO {

    private Long sampleId;

    private Long taskId;

    private Long templateId;

    private String reportType;

    private String reportName;
}
