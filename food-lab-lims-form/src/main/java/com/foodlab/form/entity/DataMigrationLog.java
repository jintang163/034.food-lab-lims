package com.foodlab.form.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_migration_log")
public class DataMigrationLog extends BaseEntity {

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
}
