package com.foodlab.ncr.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import com.foodlab.ncr.dto.*;
import com.foodlab.ncr.entity.NcrRecord;
import com.foodlab.ncr.service.NcrService;
import com.foodlab.ncr.vo.NcrDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "不合格品处置管理", description = "复检、原因分析、纠正措施、预防措施")
@RestController
@RequestMapping("/api/ncr")
@RequiredArgsConstructor
public class NcrController {

    private final NcrService ncrService;

    @Operation(summary = "创建不合格品记录")
    @PostMapping("/create")
    public Result<NcrRecord> createNcr(@RequestBody NcrCreateDTO dto,
                                       @RequestHeader("userId") Long userId) {
        return Result.success(ncrService.createNcr(dto, userId));
    }

    @Operation(summary = "获取不合格品详情")
    @GetMapping("/{id}")
    public Result<NcrDetailVO> getNcrDetail(@PathVariable Long id) {
        return Result.success(ncrService.getNcrDetail(id));
    }

    @Operation(summary = "分页查询不合格品记录")
    @GetMapping("/page")
    public Result<PageResult<NcrRecord>> getNcrPage(NcrQueryDTO dto) {
        IPage<NcrRecord> page = ncrService.getNcrPage(dto);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "获取样品的不合格品列表")
    @GetMapping("/sample/{sampleId}")
    public Result<List<NcrRecord>> getNcrListBySampleId(@PathVariable Long sampleId) {
        return Result.success(ncrService.getNcrListBySampleId(sampleId));
    }

    @Operation(summary = "提交复检结果")
    @PostMapping("/recheck/submit")
    public Result<Void> submitRecheck(@RequestBody NcrRecheckSubmitDTO dto,
                                      @RequestHeader("userId") Long userId) {
        ncrService.submitRecheck(dto, userId);
        return Result.success();
    }

    @Operation(summary = "提交原因分析")
    @PostMapping("/cause-analysis/submit")
    public Result<Void> submitCauseAnalysis(@RequestBody NcrCauseAnalysisSubmitDTO dto,
                                            @RequestHeader("userId") Long userId) {
        ncrService.submitCauseAnalysis(dto, userId);
        return Result.success();
    }

    @Operation(summary = "提交纠正措施")
    @PostMapping("/corrective-action/submit")
    public Result<Void> submitCorrectiveAction(@RequestBody NcrActionSubmitDTO dto,
                                               @RequestHeader("userId") Long userId) {
        ncrService.submitCorrectiveAction(dto, userId);
        return Result.success();
    }

    @Operation(summary = "提交预防措施")
    @PostMapping("/preventive-action/submit")
    public Result<Void> submitPreventiveAction(@RequestBody NcrActionSubmitDTO dto,
                                               @RequestHeader("userId") Long userId) {
        ncrService.submitPreventiveAction(dto, userId);
        return Result.success();
    }

    @Operation(summary = "关闭不合格品流程")
    @PostMapping("/close")
    public Result<Void> closeNcr(@RequestBody NcrCloseDTO dto,
                                 @RequestHeader("userId") Long userId) {
        ncrService.closeNcr(dto, userId);
        return Result.success();
    }

    @Operation(summary = "取消不合格品流程")
    @PostMapping("/cancel/{id}")
    public Result<Void> cancelNcr(@PathVariable Long id,
                                  @RequestParam(required = false) String remark,
                                  @RequestHeader("userId") Long userId) {
        ncrService.cancelNcr(id, userId, remark);
        return Result.success();
    }

    @Operation(summary = "根据检测结果自动创建不合格品记录")
    @PostMapping("/auto-create/{detectResultId}")
    public Result<NcrRecord> autoCreateNcr(@PathVariable Long detectResultId,
                                           @RequestHeader("userId") Long userId) {
        return Result.success(ncrService.autoCreateNcrFromDetectResult(detectResultId, userId));
    }
}
