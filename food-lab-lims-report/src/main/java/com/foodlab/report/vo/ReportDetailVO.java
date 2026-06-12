package com.foodlab.report.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportDetailVO {

    private Long id;

    private String reportCode;

    private String reportName;

    private String reportType;

    private Long templateId;

    private Long sampleId;

    private String sampleCode;

    private String sampleName;

    private Long taskId;

    private String reportContent;

    private String reportFilePath;

    private String reportStatus;

    private LocalDate issueDate;

    private LocalDate expireDate;

    private String issuerName;

    private String sealStatus;

    private String sealFilePath;

    private Integer downloadCount;

    private LocalDateTime createTime;

    private List<ReportResultItem> resultItems;

    @Data
    public static class ReportResultItem {
        private String detectItemName;
        private String detectMethod;
        private String detectStandard;
        private String resultValue;
        private String resultUnit;
        private String limitValue;
        private String finalJudge;
    }
}
