package com.foodlab.report.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import com.foodlab.report.dto.ReportGenerateDTO;
import com.foodlab.report.entity.DetectReport;
import com.foodlab.report.service.ReportService;
import com.foodlab.report.vo.ReportDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "报告管理", description = "检测报告生成、预览、导出")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "生成检测报告")
    @PostMapping("/generate")
    public Result<DetectReport> generateReport(@RequestBody ReportGenerateDTO dto,
                                               @RequestHeader("userId") Long userId) {
        return Result.success(reportService.generateReport(dto, userId));
    }

    @Operation(summary = "获取报告详情")
    @GetMapping("/{id}")
    public Result<ReportDetailVO> getReportDetail(@PathVariable Long id) {
        return Result.success(reportService.getReportDetail(id));
    }

    @Operation(summary = "获取样品的报告列表")
    @GetMapping("/sample/{sampleId}")
    public Result<List<DetectReport>> getReportsBySampleId(@PathVariable Long sampleId) {
        return Result.success(reportService.getReportsBySampleId(sampleId));
    }

    @Operation(summary = "分页查询报告")
    @GetMapping("/page")
    public Result<PageResult<DetectReport>> getReportPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String reportCode,
            @RequestParam(required = false) String sampleCode,
            @RequestParam(required = false) String reportStatus,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<DetectReport> page = reportService.getReportPage(
                pageNum, pageSize, reportCode, sampleCode, reportStatus, reportType, startDate, endDate);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "签发报告")
    @PostMapping("/issue/{id}")
    public Result<Void> issueReport(@PathVariable Long id,
                                    @RequestHeader("userId") Long userId,
                                    @RequestHeader("userName") String userName) {
        reportService.issueReport(id, userId, userName);
        return Result.success();
    }

    @Operation(summary = "预览报告")
    @GetMapping("/preview/{id}")
    public void previewReport(@PathVariable Long id, HttpServletResponse response) {
        reportService.previewReport(id, response);
    }

    @Operation(summary = "导出报告")
    @GetMapping("/export/{id}")
    public void exportReport(@PathVariable Long id, HttpServletResponse response) {
        reportService.exportReport(id, response);
    }
}
