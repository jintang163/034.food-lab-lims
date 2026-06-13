package com.foodlab.form.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodlab.form.entity.FormData;

import java.util.List;

public interface FormDataService extends IService<FormData> {

    IPage<FormData> getDataPage(int pageNum, int pageSize, Long templateId, Long sampleId, Long taskId, String status);

    FormData saveFormData(FormData formData);

    FormData submitFormData(FormData formData);

    List<FormData> getDataByTemplateAndVersion(Long templateId, Integer version);

    void validateFormData(String formSchema, String formData);

    void syncDataFromOffline(List<FormData> dataList);
}
