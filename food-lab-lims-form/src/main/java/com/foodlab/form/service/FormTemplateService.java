package com.foodlab.form.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodlab.form.entity.FormTemplate;
import com.foodlab.form.entity.FormTemplateVersion;

import java.util.List;

public interface FormTemplateService extends IService<FormTemplate> {

    IPage<FormTemplate> getTemplatePage(int pageNum, int pageSize, String templateName, String status, Long detectItemId);

    FormTemplate getCurrentTemplate(Long detectItemId);

    FormTemplate publishTemplate(Long id, String changeSummary);

    FormTemplate createNewVersion(Long id, FormTemplate template, String changeSummary);

    List<FormTemplateVersion> getVersionHistory(Long templateId);

    FormTemplate rollbackVersion(Long templateId, Long versionId);

    void validateFormSchema(String formSchema);
}
