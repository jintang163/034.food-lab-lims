package com.foodlab.detect.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.common.domain.OfflineSyncResult;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import com.foodlab.detect.dto.DetectResultSubmitDTO;
import com.foodlab.detect.dto.DetectResultSyncDTO;
import com.foodlab.detect.entity.DetectResult;
import com.foodlab.detect.service.DetectResultService;
import com.foodlab.detect.vo.DetectResultDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "检测结果管理", description = "结果录入、自动判定、审核流转")
@RestController
@RequestMapping("/api/detect/result")
@RequiredArgsConstructor
public class DetectResultController {

    private final DetectResultService detectResultService;

    @Operation(summary = "提交检测结果")
    @PostMapping("/submit")
    public Result<DetectResult> submitResult(@RequestBody DetectResultSubmitDTO dto,
                                             @RequestHeader("userId") Long userId) {
        return Result.success(detectResultService.submitResult(dto, userId));
    }

    @Operation(summary = "批量提交检测结果")
    @PostMapping("/batch-submit")
    public Result<Void> batchSubmit(@RequestBody List<DetectResultSubmitDTO> dtoList,
                                    @RequestHeader("userId") Long userId) {
        detectResultService.batchSubmit(dtoList, userId);
        return Result.success();
    }

    @Operation(summary = "获取检测结果详情")
    @GetMapping("/{id}")
    public Result<DetectResultDetailVO> getResultDetail(@PathVariable Long id) {
        return Result.success(detectResultService.getResultDetail(id));
    }

    @Operation(summary = "获取任务的检测结果列表")
    @GetMapping("/task/{taskId}")
    public Result<List<DetectResultDetailVO>> getResultsByTaskId(@PathVariable Long taskId) {
        return Result.success(detectResultService.getResultsByTaskId(taskId));
    }

    @Operation(summary = "获取样品的检测结果列表")
    @GetMapping("/sample/{sampleId}")
    public Result<List<DetectResultDetailVO>> getResultsBySampleId(@PathVariable Long sampleId) {
        return Result.success(detectResultService.getResultsBySampleId(sampleId));
    }

    @Operation(summary = "分页查询检测结果")
    @GetMapping("/page")
    public Result<PageResult<DetectResult>> getResultPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String sampleCode,
            @RequestParam(required = false) String detectItemName,
            @RequestParam(required = false) String finalJudge,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<DetectResult> page = detectResultService.getResultPage(
                pageNum, pageSize, sampleCode, detectItemName, finalJudge, startDate, endDate);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "自动判定检测结果")
    @PostMapping("/auto-judge")
    public Result<String> autoJudge(@RequestParam Long detectItemId,
                                    @RequestBody DetectResultSubmitDTO dto) {
        return Result.success(detectResultService.autoJudge(detectItemId, dto));
    }

    @Operation(summary = "离线同步检测结果")
    @PostMapping("/sync")
    public Result<OfflineSyncResult> syncOfflineData(@RequestBody DetectResultSyncDTO syncDTO) {
        return Result.success(detectResultService.syncOfflineData(syncDTO));
    }

    @Operation(summary = "导入检测结果（Excel）")
    @PostMapping("/import")
    public Result<Void> importResult(@RequestParam("file") MultipartFile file,
                                     @RequestHeader("userId") Long userId) {
        return Result.success();
    }

    @Operation(summary = "导出检测结果（Excel）")
    @GetMapping("/export")
    public void exportResult(@RequestParam(required = false) List<Long> ids,
                             HttpServletResponse response) {
    }
}
