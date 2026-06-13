package com.foodlab.form.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodlab.common.domain.OfflineSyncData;
import com.foodlab.common.domain.OfflineSyncResult;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import com.foodlab.form.dto.FormDataQueryDTO;
import com.foodlab.form.dto.FormDataSaveDTO;
import com.foodlab.form.entity.FormData;
import com.foodlab.form.service.FormDataService;
import com.foodlab.form.vo.FormDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "表单数据管理", description = "表单数据的保存、提交、查询、离线同步")
@RestController
@RequestMapping("/api/form/data")
@RequiredArgsConstructor
@DS("form")
public class FormDataController {

    private final FormDataService formDataService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "分页查询数据")
    @GetMapping("/page")
    public Result<PageResult<FormData>> getDataPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            FormDataQueryDTO queryDTO) {
        IPage<FormData> page = formDataService.getDataPage(
                pageNum, pageSize, queryDTO.getTemplateId(),
                queryDTO.getSampleId(), queryDTO.getTaskId(), queryDTO.getStatus());
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "获取数据详情")
    @GetMapping("/{id}")
    public Result<FormDataVO> getDataDetail(@PathVariable Long id) throws JsonProcessingException {
        FormData formData = formDataService.getById(id);
        FormDataVO vo = BeanUtil.copyProperties(formData, FormDataVO.class);
        if (StrUtil.isNotBlank(formData.getFormData())) {
            vo.setParsedFormData(objectMapper.readValue(formData.getFormData(), Object.class));
        }
        return Result.success(vo);
    }

    @Operation(summary = "保存表单数据（草稿）")
    @PostMapping("/save")
    public Result<FormDataVO> saveFormData(@RequestBody FormDataSaveDTO saveDTO) {
        FormData formData = BeanUtil.copyProperties(saveDTO, FormData.class);
        FormData saved = formDataService.saveFormData(formData);
        FormDataVO vo = BeanUtil.copyProperties(saved, FormDataVO.class);
        return Result.success(vo);
    }

    @Operation(summary = "提交表单数据")
    @PostMapping("/submit")
    public Result<FormDataVO> submitFormData(@RequestBody FormDataSaveDTO saveDTO) {
        FormData formData = BeanUtil.copyProperties(saveDTO, FormData.class);
        FormData submitted = formDataService.submitFormData(formData);
        FormDataVO vo = BeanUtil.copyProperties(submitted, FormDataVO.class);
        return Result.success(vo);
    }

    @Operation(summary = "离线数据同步")
    @PostMapping("/sync")
    public Result<OfflineSyncResult> syncData(@RequestBody OfflineSyncData<FormData> syncData) {
        formDataService.syncDataFromOffline(syncData.getDataList());
        OfflineSyncResult result = new OfflineSyncResult();
        result.setSuccess(true);
        result.setMessage("同步成功");
        result.setSuccessCount(syncData.getDataList().size());
        result.setFailCount(0);
        result.setSuccessIds(new ArrayList<>());
        result.setFailItems(new ArrayList<>());
        return Result.success(result);
    }

    @Operation(summary = "按模板版本查询数据")
    @GetMapping("/template/{templateId}/version/{version}")
    public Result<List<FormDataVO>> getDataByTemplateAndVersion(
            @PathVariable Long templateId,
            @PathVariable Integer version) {
        List<FormData> dataList = formDataService.getDataByTemplateAndVersion(templateId, version);
        List<FormDataVO> voList = BeanUtil.copyToList(dataList, FormDataVO.class);
        return Result.success(voList);
    }
}
