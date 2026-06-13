package com.foodlab.form.service.impl;

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
import com.foodlab.common.utils.CodeGenerator;
import com.foodlab.form.entity.FormData;
import com.foodlab.form.entity.FormTemplate;
import com.foodlab.form.mapper.FormDataMapper;
import com.foodlab.form.mapper.FormTemplateMapper;
import com.foodlab.form.service.FormDataService;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@DS("form")
@RequiredArgsConstructor
public class FormDataServiceImpl extends ServiceImpl<FormDataMapper, FormData> implements FormDataService {

    private final FormDataMapper formDataMapper;
    private final FormTemplateMapper formTemplateMapper;
    private final FormTemplateService formTemplateService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public IPage<FormData> getDataPage(int pageNum, int pageSize, Long templateId, Long sampleId, Long taskId, String status) {
        Page<FormData> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FormData> wrapper = new LambdaQueryWrapper<>();
        if (templateId != null) {
            wrapper.eq(FormData::getTemplateId, templateId);
        }
        if (sampleId != null) {
            wrapper.eq(FormData::getSampleId, sampleId);
        }
        if (taskId != null) {
            wrapper.eq(FormData::getTaskId, taskId);
        }
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(FormData::getStatus, status);
        }
        wrapper.orderByDesc(FormData::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormData saveFormData(FormData formData) {
        FormTemplate template = formTemplateMapper.selectById(formData.getTemplateId());
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_TEMPLATE_NOT_FOUND);
        }

        validateFormData(template.getFormSchema(), formData.getFormData());

        if (formData.getId() == null) {
            formData.setDataCode(CodeGenerator.generateCode("FD"));
            formData.setTemplateCode(template.getTemplateCode());
            formData.setTemplateVersion(template.getVersion());
            formData.setDetectItemId(template.getDetectItemId());
            formData.setStatus("draft");
            formData.setSyncStatus("success");
            formData.setCreateBy(1L);
            formData.setUpdateBy(1L);
            save(formData);
        } else {
            FormData existData = getById(formData.getId());
            if (existData == null) {
                throw new BusinessException(ResultCode.FORM_DATA_NOT_FOUND);
            }
            if (!"draft".equals(existData.getStatus())) {
                throw new BusinessException(ResultCode.FORM_TEMPLATE_STATUS_ERROR, "只有草稿状态的数据才能修改");
            }
            formData.setUpdateBy(1L);
            updateById(formData);
        }

        return formData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormData submitFormData(FormData formData) {
        FormTemplate template = formTemplateMapper.selectById(formData.getTemplateId());
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_TEMPLATE_NOT_FOUND);
        }
        if (!"published".equals(template.getStatus())) {
            throw new BusinessException(ResultCode.FORM_TEMPLATE_STATUS_ERROR, "只能提交已发布模板的数据");
        }

        validateFormData(template.getFormSchema(), formData.getFormData());

        if (formData.getId() == null) {
            formData.setDataCode(CodeGenerator.generateCode("FD"));
            formData.setTemplateCode(template.getTemplateCode());
            formData.setTemplateVersion(template.getVersion());
            formData.setDetectItemId(template.getDetectItemId());
            formData.setCreateBy(1L);
        }

        formData.setStatus("submitted");
        formData.setSubmitTime(LocalDateTime.now());
        formData.setSubmittedBy(1L);
        formData.setSubmittedByName("当前用户");
        formData.setSyncStatus("success");
        formData.setUpdateBy(1L);
        saveOrUpdate(formData);

        return formData;
    }

    @Override
    public List<FormData> getDataByTemplateAndVersion(Long templateId, Integer version) {
        return formDataMapper.selectByTemplateIdAndVersion(templateId, version);
    }

    @Override
    public void validateFormData(String formSchema, String formData) {
        if (StrUtil.isBlank(formSchema)) {
            throw new BusinessException(ResultCode.FORM_SCHEMA_INVALID, "表单Schema不能为空");
        }
        if (StrUtil.isBlank(formData)) {
            throw new BusinessException(ResultCode.FORM_DATA_INVALID, "表单数据不能为空");
        }
        try {
            JsonNode schemaNode = objectMapper.readTree(formSchema);
            JsonNode dataNode = objectMapper.readTree(formData);
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            JsonSchema schema = factory.getSchema(schemaNode);
            Set<ValidationMessage> errors = schema.validate(dataNode);
            if (!errors.isEmpty()) {
                StringBuilder sb = new StringBuilder("表单数据验证失败:");
                for (ValidationMessage error : errors) {
                    sb.append("\n").append(error.getMessage());
                }
                throw new BusinessException(ResultCode.FORM_DATA_INVALID, sb.toString());
            }
            log.debug("表单数据验证通过");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("表单数据验证失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FORM_DATA_INVALID, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDataFromOffline(List<FormData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        List<FormData> successList = new ArrayList<>();
        List<String> failMessages = new ArrayList<>();

        for (FormData data : dataList) {
            try {
                if (StrUtil.isNotBlank(data.getOfflineId())) {
                    FormData existData = formDataMapper.selectByOfflineId(data.getOfflineId());
                    if (existData != null) {
                        data.setId(existData.getId());
                    }
                }

                FormTemplate template = formTemplateMapper.selectById(data.getTemplateId());
                if (template == null) {
                    throw new BusinessException(ResultCode.FORM_TEMPLATE_NOT_FOUND);
                }

                validateFormData(template.getFormSchema(), data.getFormData());

                if (data.getId() == null) {
                    data.setDataCode(CodeGenerator.generateCode("FD"));
                    data.setTemplateCode(template.getTemplateCode());
                    data.setTemplateVersion(template.getVersion());
                    data.setDetectItemId(template.getDetectItemId());
                    if (!"submitted".equals(data.getStatus())) {
                        data.setStatus("draft");
                    }
                    data.setSyncStatus("success");
                    data.setCreateBy(1L);
                    data.setUpdateBy(1L);
                } else {
                    data.setUpdateBy(1L);
                }

                successList.add(data);
            } catch (Exception e) {
                log.error("离线数据同步失败: {}", e.getMessage(), e);
                failMessages.add("数据[" + data.getOfflineId() + "]同步失败: " + e.getMessage());
            }
        }

        if (!successList.isEmpty()) {
            saveOrUpdateBatch(successList);
        }

        if (!failMessages.isEmpty()) {
            throw new BusinessException(ResultCode.DATA_SYNC_FAIL, String.join("\n", failMessages));
        }
    }
}
