package com.foodlab.form.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.common.result.ResultCode;
import com.foodlab.form.entity.DataMigrationLog;
import com.foodlab.form.entity.FormData;
import com.foodlab.form.entity.FormTemplate;
import com.foodlab.form.entity.FormTemplateVersion;
import com.foodlab.form.mapper.DataMigrationLogMapper;
import com.foodlab.form.mapper.FormDataMapper;
import com.foodlab.form.mapper.FormTemplateMapper;
import com.foodlab.form.mapper.FormTemplateVersionMapper;
import com.foodlab.form.service.DataMigrationService;
import com.foodlab.form.service.FormDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@DS("form")
@RequiredArgsConstructor
public class DataMigrationServiceImpl implements DataMigrationService {

    private final DataMigrationLogMapper dataMigrationLogMapper;
    private final FormDataMapper formDataMapper;
    private final FormTemplateMapper formTemplateMapper;
    private final FormTemplateVersionMapper formTemplateVersionMapper;
    private final FormDataService formDataService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataMigrationLog startMigration(Long templateId, Integer fromVersion, Integer toVersion, String migrationRule) {
        FormTemplate template = formTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_TEMPLATE_NOT_FOUND);
        }

        FormTemplateVersion fromVersionEntity = formTemplateVersionMapper.selectByTemplateIdAndVersion(templateId, fromVersion);
        if (fromVersionEntity == null) {
            throw new BusinessException(ResultCode.FORM_TEMPLATE_VERSION_NOT_FOUND, "源版本不存在");
        }

        FormTemplateVersion toVersionEntity = formTemplateVersionMapper.selectByTemplateIdAndVersion(templateId, toVersion);
        if (toVersionEntity == null) {
            throw new BusinessException(ResultCode.FORM_TEMPLATE_VERSION_NOT_FOUND, "目标版本不存在");
        }

        List<FormData> migratableData = getMigratableData(templateId, fromVersion);

        DataMigrationLog migrationLog = new DataMigrationLog();
        migrationLog.setTemplateId(templateId);
        migrationLog.setFromVersion(fromVersion);
        migrationLog.setToVersion(toVersion);
        migrationLog.setMigrationRule(migrationRule);
        migrationLog.setTotalCount(migratableData.size());
        migrationLog.setSuccessCount(0);
        migrationLog.setFailedCount(0);
        migrationLog.setStatus("pending");
        migrationLog.setStartTime(LocalDateTime.now());
        migrationLog.setCreateBy(1L);
        migrationLog.setUpdateBy(1L);
        dataMigrationLogMapper.insert(migrationLog);

        executeMigrationAsync(migrationLog.getId(), migratableData, toVersionEntity.getFormSchema(), migrationRule);

        return migrationLog;
    }

    @Override
    public DataMigrationLog getMigrationStatus(Long migrationId) {
        DataMigrationLog log = dataMigrationLogMapper.selectById(migrationId);
        if (log == null) {
            throw new BusinessException(ResultCode.MIGRATION_NOT_FOUND);
        }
        return log;
    }

    @Override
    public List<FormData> getMigratableData(Long templateId, Integer fromVersion) {
        return formDataMapper.selectByTemplateIdAndVersion(templateId, fromVersion);
    }

    @Async
    @Transactional(rollbackFor = Exception.class)
    public void executeMigrationAsync(Long migrationId, List<FormData> dataList, String toSchema, String migrationRule) {
        DataMigrationLog migrationLog = dataMigrationLogMapper.selectById(migrationId);
        if (migrationLog == null) {
            log.error("迁移记录不存在: {}", migrationId);
            return;
        }

        try {
            migrationLog.setStatus("running");
            dataMigrationLogMapper.updateById(migrationLog);

            int successCount = 0;
            int failedCount = 0;
            List<Long> failedDataIds = new ArrayList<>();

            for (FormData data : dataList) {
                try {
                    String migratedData = migrateData(data.getFormData(), migrationRule);
                    formDataService.validateFormData(toSchema, migratedData);

                    FormData newData = new FormData();
                    newData.setTemplateId(data.getTemplateId());
                    newData.setTemplateCode(data.getTemplateCode());
                    newData.setTemplateVersion(migrationLog.getToVersion());
                    newData.setDetectItemId(data.getDetectItemId());
                    newData.setSampleId(data.getSampleId());
                    newData.setSampleCode(data.getSampleCode());
                    newData.setTaskId(data.getTaskId());
                    newData.setFormData(migratedData);
                    newData.setStatus(data.getStatus());
                    newData.setSubmitTime(data.getSubmitTime());
                    newData.setSubmittedBy(data.getSubmittedBy());
                    newData.setSubmittedByName(data.getSubmittedByName());
                    newData.setSyncStatus("success");
                    newData.setCreateBy(1L);
                    newData.setUpdateBy(1L);
                    formDataMapper.insert(newData);

                    successCount++;
                } catch (Exception e) {
                    log.error("数据迁移失败, 数据ID: {}, 错误: {}", data.getId(), e.getMessage(), e);
                    failedCount++;
                    failedDataIds.add(data.getId());
                }
            }

            migrationLog.setSuccessCount(successCount);
            migrationLog.setFailedCount(failedCount);
            migrationLog.setFailedDataIds(objectMapper.writeValueAsString(failedDataIds));
            migrationLog.setStatus(failedCount == 0 ? "success" : "partial");
            migrationLog.setEndTime(LocalDateTime.now());
            migrationLog.setUpdateBy(1L);
            dataMigrationLogMapper.updateById(migrationLog);

            log.info("数据迁移完成, 迁移ID: {}, 成功: {}, 失败: {}", migrationId, successCount, failedCount);
        } catch (Exception e) {
            log.error("数据迁移异常, 迁移ID: {}", migrationId, e);
            migrationLog.setStatus("failed");
            migrationLog.setErrorMessage(e.getMessage());
            migrationLog.setEndTime(LocalDateTime.now());
            migrationLog.setUpdateBy(1L);
            dataMigrationLogMapper.updateById(migrationLog);
        }
    }

    private String migrateData(String originalData, String migrationRule) {
        try {
            JsonNode dataNode = objectMapper.readTree(originalData);
            if (migrationRule == null || migrationRule.isEmpty()) {
                return originalData;
            }
            JsonNode ruleNode = objectMapper.readTree(migrationRule);
            return dataNode.toString();
        } catch (Exception e) {
            log.warn("数据迁移规则处理失败，使用原始数据: {}", e.getMessage());
            return originalData;
        }
    }
}
