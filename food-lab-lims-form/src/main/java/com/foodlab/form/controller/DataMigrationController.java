package com.foodlab.form.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.foodlab.common.result.Result;
import com.foodlab.form.dto.DataMigrationDTO;
import com.foodlab.form.entity.DataMigrationLog;
import com.foodlab.form.entity.FormData;
import com.foodlab.form.service.DataMigrationService;
import com.foodlab.form.vo.FormDataVO;
import com.foodlab.form.vo.MigrationStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据迁移管理", description = "表单模板版本升级时的数据迁移")
@RestController
@RequestMapping("/api/form/migration")
@RequiredArgsConstructor
@DS("form")
public class DataMigrationController {

    private final DataMigrationService dataMigrationService;

    @Operation(summary = "启动迁移")
    @PostMapping("/start")
    public Result<MigrationStatusVO> startMigration(@RequestBody DataMigrationDTO migrationDTO) {
        DataMigrationLog migrationLog = dataMigrationService.startMigration(
                migrationDTO.getTemplateId(),
                migrationDTO.getFromVersion(),
                migrationDTO.getToVersion(),
                migrationDTO.getMigrationRule());
        MigrationStatusVO vo = BeanUtil.copyProperties(migrationLog, MigrationStatusVO.class);
        return Result.success(vo);
    }

    @Operation(summary = "查询迁移状态")
    @GetMapping("/{id}/status")
    public Result<MigrationStatusVO> getMigrationStatus(@PathVariable Long id) {
        DataMigrationLog migrationLog = dataMigrationService.getMigrationStatus(id);
        MigrationStatusVO vo = BeanUtil.copyProperties(migrationLog, MigrationStatusVO.class);
        return Result.success(vo);
    }

    @Operation(summary = "获取可迁移数据列表")
    @GetMapping("/migratable")
    public Result<List<FormDataVO>> getMigratableData(
            @RequestParam Long templateId,
            @RequestParam Integer fromVersion) {
        List<FormData> dataList = dataMigrationService.getMigratableData(templateId, fromVersion);
        List<FormDataVO> voList = BeanUtil.copyToList(dataList, FormDataVO.class);
        return Result.success(voList);
    }
}
