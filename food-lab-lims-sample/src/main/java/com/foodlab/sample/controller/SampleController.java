package com.foodlab.sample.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.common.domain.OfflineSyncResult;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import com.foodlab.sample.dto.SampleRegisterDTO;
import com.foodlab.sample.dto.SampleSyncDTO;
import com.foodlab.sample.entity.Sample;
import com.foodlab.sample.service.SampleService;
import com.foodlab.sample.vo.SampleDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "样品管理")
@RestController
@RequestMapping("/api/sample")
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    @Operation(summary = "样品登记")
    @PostMapping("/register")
    public Result<Sample> registerSample(@Valid @RequestBody SampleRegisterDTO dto,
                                         @RequestHeader("userId") Long userId) {
        Sample sample = sampleService.registerSample(dto, userId);
        return Result.success(sample);
    }

    @Operation(summary = "获取样品详情")
    @GetMapping("/{id}")
    public Result<SampleDetailVO> getSampleDetail(@PathVariable Long id) {
        SampleDetailVO detail = sampleService.getSampleDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "分页查询样品列表")
    @GetMapping("/page")
    public Result<PageResult<Sample>> getSamplePage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String sampleName,
            @RequestParam(required = false) String sampleCode,
            @RequestParam(required = false) String sampleStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<Sample> page = sampleService.getSamplePage(pageNum, pageSize, sampleName,
                sampleCode, sampleStatus, startDate, endDate);
        PageResult<Sample> pageResult = PageResult.of(page.getRecords(), page.getTotal(),
                page.getSize(), page.getCurrent());
        return Result.success(pageResult);
    }

    @Operation(summary = "更新样品信息")
    @PutMapping("/{id}")
    public Result<Void> updateSample(@PathVariable Long id,
                                     @RequestBody SampleRegisterDTO dto) {
        sampleService.updateSample(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除样品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSample(@PathVariable Long id) {
        sampleService.deleteSample(id);
        return Result.success();
    }

    @Operation(summary = "批量删除样品")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        sampleService.batchDelete(ids);
        return Result.success();
    }

    @Operation(summary = "离线数据同步")
    @PostMapping("/sync")
    public Result<OfflineSyncResult> syncOfflineData(@RequestBody SampleSyncDTO syncDTO) {
        OfflineSyncResult result = sampleService.syncOfflineData(syncDTO);
        return Result.success(result);
    }

    @Operation(summary = "生成条形码")
    @GetMapping("/{id}/barcode")
    public Result<String> generateBarcode(@PathVariable Long id) {
        String barcode = sampleService.generateBarcode(id);
        return Result.success(barcode);
    }

    @Operation(summary = "生成二维码")
    @GetMapping("/{id}/qrcode")
    public Result<String> generateQRCode(@PathVariable Long id) {
        String qrCode = sampleService.generateQRCode(id);
        return Result.success(qrCode);
    }

    @Operation(summary = "导出样品数据")
    @GetMapping("/export")
    public void exportSample(@RequestParam(required = false) List<Long> ids,
                             HttpServletResponse response) {
        sampleService.exportSample(ids, response);
    }

    @Operation(summary = "导入样品数据")
    @PostMapping("/import")
    public Result<Void> importSample(@RequestParam("file") MultipartFile file,
                                     @RequestHeader("userId") Long userId) {
        sampleService.importSample(file, userId);
        return Result.success("导入成功", null);
    }
}
