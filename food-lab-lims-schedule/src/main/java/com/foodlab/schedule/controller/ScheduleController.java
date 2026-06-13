package com.foodlab.schedule.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import com.foodlab.schedule.dto.ScheduleAdjustDTO;
import com.foodlab.schedule.dto.ScheduleGenerateDTO;
import com.foodlab.schedule.dto.ScheduleQueryDTO;
import com.foodlab.schedule.service.ScheduleService;
import com.foodlab.schedule.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "智能排程管理")
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "生成排程")
    @PostMapping("/generate")
    public Result<String> generateSchedule(
            @Valid @RequestBody ScheduleGenerateDTO dto,
            @RequestHeader("userId") Long userId) {
        String batchNo = scheduleService.generateSchedule(dto, userId);
        return Result.success(batchNo);
    }

    @Operation(summary = "分页查询排程结果")
    @GetMapping("/page")
    public Result<PageResult<ScheduleResultVO>> getSchedulePage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            ScheduleQueryDTO queryDTO) {
        IPage<ScheduleResultVO> page = scheduleService.getSchedulePage(pageNum, pageSize, queryDTO);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "查询排程列表")
    @GetMapping("/list")
    public Result<List<ScheduleResultVO>> getScheduleList(ScheduleQueryDTO queryDTO) {
        return Result.success(scheduleService.getScheduleList(queryDTO));
    }

    @Operation(summary = "获取甘特图数据")
    @GetMapping("/gantt")
    public Result<List<ScheduleGanttVO>> getGanttData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "instrument") String groupBy) {
        return Result.success(scheduleService.getGanttData(startDate, endDate, groupBy));
    }

    @Operation(summary = "获取甘特图资源（仪器/人员）")
    @GetMapping("/gantt/resources")
    public Result<List<GanttResourceVO>> getGanttResources(
            @RequestParam(defaultValue = "all") String type) {
        return Result.success(scheduleService.getGanttResources(type));
    }

    @Operation(summary = "检查排程冲突")
    @GetMapping("/conflicts")
    public Result<List<ScheduleConflictVO>> checkConflicts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(scheduleService.checkConflicts(startDate, endDate));
    }

    @Operation(summary = "调整排程（拖拽修改）")
    @PostMapping("/adjust")
    public Result<Boolean> adjustSchedule(
            @Valid @RequestBody ScheduleAdjustDTO dto,
            @RequestHeader("userId") Long userId) {
        return Result.success(scheduleService.adjustSchedule(dto, userId));
    }

    @Operation(summary = "取消排程")
    @PostMapping("/cancel/{id}")
    public Result<Boolean> cancelSchedule(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestHeader("userId") Long userId) {
        return Result.success(scheduleService.cancelSchedule(id, reason, userId));
    }

    @Operation(summary = "发布排程（同步到仪器日历）")
    @PostMapping("/publish/{batchNo}")
    public Result<Boolean> publishSchedule(
            @PathVariable String batchNo,
            @RequestHeader("userId") Long userId) {
        return Result.success(scheduleService.publishSchedule(batchNo, userId));
    }

    @Operation(summary = "按批次获取排程")
    @GetMapping("/batch/{batchNo}")
    public Result<List<ScheduleResultVO>> getScheduleByBatchNo(@PathVariable String batchNo) {
        return Result.success(scheduleService.getScheduleByBatchNo(batchNo));
    }

    @Operation(summary = "获取排程详情")
    @GetMapping("/{id}")
    public Result<ScheduleResultVO> getScheduleDetail(@PathVariable Long id) {
        return Result.success(scheduleService.getScheduleDetail(id));
    }
}
