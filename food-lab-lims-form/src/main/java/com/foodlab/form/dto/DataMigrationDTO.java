package com.foodlab.form.dto;

import lombok.Data;

@Data
public class DataMigrationDTO {

    private Long templateId;

    private Integer fromVersion;

    private Integer toVersion;

    private String migrationRule;
}
