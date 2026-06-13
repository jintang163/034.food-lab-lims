package com.foodlab.schedule.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import com.foodlab.schedule.entity.Instrument;
import com.foodlab.schedule.entity.InstrumentCalendar;
import com.foodlab.schedule.service.InstrumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "仪器设备管理")
@RestController
@RequestMapping("/api/instrument")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentService instrumentService;

    @Operation(summary = "分页查询仪器")
    @GetMapping("/page")
    public Result<PageResult<Instrument>> getInstrumentPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        IPage<Instrument> page = instrumentService.getInstrumentPage(pageNum, pageSize, keyword, status);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "获取仪器详情")
    @GetMapping("/{id}")
    public Result<Instrument> getInstrumentById(@PathVariable Long id) {
        return Result.success(instrumentService.getInstrumentById(id));
    }

    @Operation(summary = "新增仪器")
    @PostMapping
    public Result<Boolean> saveInstrument(
            @RequestBody Instrument instrument,
            @RequestHeader("userId") Long userId) {
        instrument.setCreateBy(userId);
        instrument.setUpdateBy(userId);
        return Result.success(instrumentService.saveInstrument(instrument));
    }

    @Operation(summary = "更新仪器")
    @PutMapping
    public Result<Boolean> updateInstrument(
            @RequestBody Instrument instrument,
            @RequestHeader("userId") Long userId) {
        instrument.setUpdateBy(userId);
        return Result.success(instrumentService.updateInstrument(instrument));
    }

    @Operation(summary = "删除仪器")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteInstrument(@PathVariable Long id) {
        return Result.success(instrumentService.deleteInstrument(id));
    }

    @Operation(summary = "按检测项目获取可用仪器")
    @GetMapping("/by-detect-item/{detectItemId}")
    public Result<List<Instrument>> getAvailableInstrumentsByDetectItem(@PathVariable Long detectItemId) {
        return Result.success(instrumentService.getAvailableInstrumentsByDetectItem(detectItemId));
    }

    @Operation(summary = "查询仪器日历")
    @GetMapping("/{id}/calendar")
    public Result<List<InstrumentCalendar>> getInstrumentCalendar(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(instrumentService.getInstrumentCalendar(id, startDate, endDate));
    }

    @Operation(summary = "新增仪器日历事件")
    @PostMapping("/calendar")
    public Result<Boolean> addCalendarEvent(
            @RequestBody InstrumentCalendar calendar,
            @RequestHeader("userId") Long userId) {
        calendar.setCreateBy(userId);
        calendar.setUpdateBy(userId);
        return Result.success(instrumentService.addCalendarEvent(calendar));
    }

    @Operation(summary = "更新仪器日历事件")
    @PutMapping("/calendar")
    public Result<Boolean> updateCalendarEvent(
            @RequestBody InstrumentCalendar calendar,
            @RequestHeader("userId") Long userId) {
        calendar.setUpdateBy(userId);
        return Result.success(instrumentService.updateCalendarEvent(calendar));
    }

    @Operation(summary = "删除仪器日历事件")
    @DeleteMapping("/calendar/{id}")
    public Result<Boolean> deleteCalendarEvent(@PathVariable Long id) {
        return Result.success(instrumentService.deleteCalendarEvent(id));
    }
}
