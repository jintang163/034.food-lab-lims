package com.foodlab.form.service;

import com.foodlab.form.entity.DataMigrationLog;
import com.foodlab.form.entity.FormData;

import java.util.List;

public interface DataMigrationService {

    DataMigrationLog startMigration(Long templateId, Integer fromVersion, Integer toVersion, String migrationRule);

    DataMigrationLog getMigrationStatus(Long migrationId);

    List<FormData> getMigratableData(Long templateId, Integer fromVersion);
}
