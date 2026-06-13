package com.foodlab.form.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.common.result.ResultCode;
import com.foodlab.form.entity.FormTemplate;
import com.foodlab.form.entity.FormTemplateVersion;
import com.foodlab.form.mapper.FormTemplateMapper;
import com.foodlab.form.mapper.FormTemplateVersionMapper;
import com.foodlab.form.service.FormTemplateService;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@DS("form")
@RequiredArgsConstructor
public class FormTemplateServiceImpl extends ServiceImpl<FormTemplateMapper, FormTemplate> implements FormTemplateService {

    private final FormTemplateMapper formTemplateMapper;
    private final FormTemplateVersionMapper formTemplateVersionMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public IPage<FormTemplate> getTemplatePage(int pageNum, int pageSize, String templateName, String status, Long detectItemId) {
        Page<FormTemplate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FormTemplate> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(templateName)) {
            wrapper.like(FormTemplate::getTemplateName, templateName);
        }
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(FormTemplate::getStatus, status);
        }
        if (detectItemId != null) {
            wrapper.eq(FormTemplate::getDetectItemId, detectItemId);
        }
        wrapper.orderByDesc(FormTemplate::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public FormTemplate getCurrentTemplate(Long detectItemId) {
        FormTemplate template = formTemplateMapper.selectCurrentByDetectItemId(detectItemId);
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_TEMPLATE_NOT_FOUND);
        }
        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormTemplate publishTemplate(Long id, String changeSummary) {
        FormTemplate template = getById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_TEMPLATE_NOT_FOUND);
        }
        if (!"draft".equals(template.getStatus())) {
            throw new BusinessException(ResultCode.FORM_TEMPLATE_STATUS_ERROR, "只有草稿状态的模板才能发布");
        }

        validateFormSchema(template.getFormSchema());

        formTemplateMapper.clearCurrentFlagByDetectItemId(template.getDetectItemId());

        template.setStatus("published");
        template.setIsCurrent(true);
        template.setPublishTime(LocalDateTime.now());
        template.setPublishedBy(1L);
        updateById(template);

        saveVersionHistory(template, "publish", changeSummary);

        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormTemplate createNewVersion(Long id, FormTemplate template, String changeSummary) {
        FormTemplate oldTemplate = getById(id);
        if (oldTemplate == null) {
            throw new BusinessException(ResultCode.FORM_TEMPLATE_NOT_FOUND);
        }

        validateFormSchema(template.getFormSchema());

        FormTemplate newTemplate = new FormTemplate();
        BeanUtil.copyProperties(template, newTemplate, "id", "version", "isCurrent", "status", "publishTime", "publishedBy", "createTime", "updateTime", "createBy", "updateBy", "deleted");
        newTemplate.setTemplateCode(oldTemplate.getTemplateCode());
        newTemplate.setVersion(oldTemplate.getVersion() + 1);
        newTemplate.setIsCurrent(false);
        newTemplate.setStatus("draft");
        newTemplate.setCreateBy(1L);
        newTemplate.setUpdateBy(1L);
        save(newTemplate);

        return newTemplate;
    }

    @Override
    public List<FormTemplateVersion> getVersionHistory(Long templateId) {
        return formTemplateVersionMapper.selectByTemplateId(templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormTemplate rollbackVersion(Long templateId, Long versionId) {
        FormTemplate template = getById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_TEMPLATE_NOT_FOUND);
        }

        FormTemplateVersion version = formTemplateVersionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException(ResultCode.FORM_TEMPLATE_VERSION_NOT_FOUND);
        }
        if (!version.getTemplateId().equals(templateId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "版本不属于该模板");
        }

        template.setFormSchema(version.getFormSchema());
        template.setTemplateName(version.getTemplateName());
        template.setVersion(version.getVersion());
        template.setStatus("draft");
        template.setUpdateBy(1L);
        updateById(template);

        saveVersionHistory(template, "rollback", "回滚到版本 " + version.getVersion());

        return template;
    }

    @Override
    public void validateFormSchema(String formSchema) {
        if (StrUtil.isBlank(formSchema)) {
            throw new BusinessException(ResultCode.FORM_SCHEMA_INVALID, "表单Schema不能为空");
        }
        try {
            JsonNode schemaNode = objectMapper.readTree(formSchema);
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            JsonSchema schema = factory.getSchema(schemaNode);
            JsonNode emptyNode = objectMapper.createObjectNode();
            Set<ValidationMessage> errors = schema.validate(emptyNode);
            log.debug("表单Schema验证通过");
        } catch (Exception e) {
            log.error("表单Schema验证失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FORM_SCHEMA_INVALID, e.getMessage());
        }
    }

    private void saveVersionHistory(FormTemplate template, String changeType, String changeSummary) {
        FormTemplateVersion version = new FormTemplateVersion();
        version.setTemplateId(template.getId());
        version.setTemplateCode(template.getTemplateCode());
        version.setTemplateName(template.getTemplateName());
        version.setDetectItemId(template.getDetectItemId());
        version.setFormSchema(template.getFormSchema());
        version.setVersion(template.getVersion());
        version.setChangeType(changeType);
        version.setChangeSummary(changeSummary);
        version.setPublishTime(template.getPublishTime());
        version.setPublishedBy(template.getPublishedBy());
        version.setCreateBy(1L);
        version.setUpdateBy(1L);
        formTemplateVersionMapper.insert(version);
    }
}
