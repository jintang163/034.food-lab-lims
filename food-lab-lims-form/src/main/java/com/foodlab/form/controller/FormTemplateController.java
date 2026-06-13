package com.foodlab.form.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import com.foodlab.form.dto.FormTemplateNewVersionDTO;
import com.foodlab.form.dto.FormTemplatePublishDTO;
import com.foodlab.form.dto.FormTemplateQueryDTO;
import com.foodlab.form.dto.FormTemplateSaveDTO;
import com.foodlab.form.entity.FormTemplate;
import com.foodlab.form.entity.FormTemplateVersion;
import com.foodlab.form.service.FormTemplateService;
import com.foodlab.form.vo.FormTemplateVO;
import com.foodlab.form.vo.FormTemplateVersionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "表单模板管理", description = "表单模板的增删改查、版本控制、发布管理")
@RestController
@RequestMapping("/api/form/template")
@RequiredArgsConstructor
@DS("form")
public class FormTemplateController {

    private final FormTemplateService formTemplateService;

    @Operation(summary = "分页查询模板")
    @GetMapping("/page")
    public Result<PageResult<FormTemplate>> getTemplatePage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            FormTemplateQueryDTO queryDTO) {
        IPage<FormTemplate> page = formTemplateService.getTemplatePage(
                pageNum, pageSize, queryDTO.getTemplateName(),
                queryDTO.getStatus(), queryDTO.getDetectItemId());
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "获取模板详情")
    @GetMapping("/{id}")
    public Result<FormTemplateVO> getTemplateDetail(@PathVariable Long id) {
        FormTemplate template = formTemplateService.getById(id);
        FormTemplateVO vo = BeanUtil.copyProperties(template, FormTemplateVO.class);
        return Result.success(vo);
    }

    @Operation(summary = "获取检测项目当前模板")
    @GetMapping("/current/{detectItemId}")
    public Result<FormTemplateVO> getCurrentTemplate(@PathVariable Long detectItemId) {
        FormTemplate template = formTemplateService.getCurrentTemplate(detectItemId);
        FormTemplateVO vo = BeanUtil.copyProperties(template, FormTemplateVO.class);
        return Result.success(vo);
    }

    @Operation(summary = "获取模板列表")
    @GetMapping("/list")
    public Result<List<FormTemplateVO>> getTemplateList(FormTemplateQueryDTO queryDTO) {
        IPage<FormTemplate> page = formTemplateService.getTemplatePage(
                1, Integer.MAX_VALUE, queryDTO.getTemplateName(),
                queryDTO.getStatus(), queryDTO.getDetectItemId());
        List<FormTemplateVO> voList = BeanUtil.copyToList(page.getRecords(), FormTemplateVO.class);
        return Result.success(voList);
    }

    @Operation(summary = "新增模板")
    @PostMapping
    public Result<Void> addTemplate(@RequestBody FormTemplateSaveDTO saveDTO) {
        FormTemplate template = BeanUtil.copyProperties(saveDTO, FormTemplate.class);
        formTemplateService.save(template);
        return Result.success();
    }

    @Operation(summary = "修改模板")
    @PutMapping("/{id}")
    public Result<Void> updateTemplate(@PathVariable Long id, @RequestBody FormTemplateSaveDTO saveDTO) {
        FormTemplate template = BeanUtil.copyProperties(saveDTO, FormTemplate.class);
        template.setId(id);
        formTemplateService.updateById(template);
        return Result.success();
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        formTemplateService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "发布模板")
    @PostMapping("/{id}/publish")
    public Result<FormTemplateVO> publishTemplate(@PathVariable Long id, @RequestBody FormTemplatePublishDTO publishDTO) {
        FormTemplate template = formTemplateService.publishTemplate(id, publishDTO.getChangeSummary());
        FormTemplateVO vo = BeanUtil.copyProperties(template, FormTemplateVO.class);
        return Result.success(vo);
    }

    @Operation(summary = "创建新版本")
    @PostMapping("/{id}/new-version")
    public Result<FormTemplateVO> createNewVersion(@PathVariable Long id, @RequestBody FormTemplateNewVersionDTO newVersionDTO) {
        FormTemplate template = new FormTemplate();
        template.setFormSchema(newVersionDTO.getFormSchema());
        FormTemplate newTemplate = formTemplateService.createNewVersion(id, template, newVersionDTO.getChangeSummary());
        FormTemplateVO vo = BeanUtil.copyProperties(newTemplate, FormTemplateVO.class);
        return Result.success(vo);
    }

    @Operation(summary = "获取版本历史")
    @GetMapping("/{id}/versions")
    public Result<List<FormTemplateVersionVO>> getVersionHistory(@PathVariable Long id) {
        List<FormTemplateVersion> versionList = formTemplateService.getVersionHistory(id);
        List<FormTemplateVersionVO> voList = BeanUtil.copyToList(versionList, FormTemplateVersionVO.class);
        return Result.success(voList);
    }

    @Operation(summary = "回滚版本")
    @PostMapping("/{id}/rollback/{versionId}")
    public Result<FormTemplateVO> rollbackVersion(@PathVariable Long id, @PathVariable Long versionId) {
        FormTemplate template = formTemplateService.rollbackVersion(id, versionId);
        FormTemplateVO vo = BeanUtil.copyProperties(template, FormTemplateVO.class);
        return Result.success(vo);
    }

    @Operation(summary = "验证Schema")
    @PostMapping("/validate-schema")
    public Result<Void> validateSchema(@RequestBody String formSchema) {
        formTemplateService.validateFormSchema(formSchema);
        return Result.success();
    }
}
