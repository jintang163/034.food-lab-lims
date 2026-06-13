package com.foodlab.form.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MigrationStatusVO {

    private Long id;

    private Long templateId;

    private Integer fromVersion;

    private Integer toVersion;

    private String migrationRule;

    private Integer totalCount;

    private Integer successCount;

    private Integer failedCount;

    private String failedDataIds;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
