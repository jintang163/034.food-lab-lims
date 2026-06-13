package com.foodlab.schedule.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import com.foodlab.schedule.entity.StaffLeave;
import com.foodlab.schedule.service.StaffLeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "人员休假管理")
@RestController
@RequestMapping("/api/staff-leave")
@RequiredArgsConstructor
public class StaffLeaveController {

    private final StaffLeaveService staffLeaveService;

    @Operation(summary = "分页查询请假记录")
    @GetMapping("/page")
    public Result<PageResult<StaffLeave>> getLeavePage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status) {
        IPage<StaffLeave> page = staffLeaveService.getLeavePage(pageNum, pageSize, userId, status);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "获取请假详情")
    @GetMapping("/{id}")
    public Result<StaffLeave> getLeaveById(@PathVariable Long id) {
        return Result.success(staffLeaveService.getLeaveById(id));
    }

    @Operation(summary = "申请请假")
    @PostMapping("/apply")
    public Result<Boolean> applyLeave(
            @RequestBody StaffLeave leave,
            @RequestHeader("userId") Long userId) {
        return Result.success(staffLeaveService.applyLeave(leave, userId));
    }

    @Operation(summary = "批准请假")
    @PostMapping("/approve/{id}")
    public Result<Boolean> approveLeave(
            @PathVariable Long id,
            @RequestParam(required = false) String remark,
            @RequestHeader("userId") Long approverId) {
        return Result.success(staffLeaveService.approveLeave(id, approverId, remark));
    }

    @Operation(summary = "拒绝请假")
    @PostMapping("/reject/{id}")
    public Result<Boolean> rejectLeave(
            @PathVariable Long id,
            @RequestParam(required = false) String remark,
            @RequestHeader("userId") Long approverId) {
        return Result.success(staffLeaveService.rejectLeave(id, approverId, remark));
    }

    @Operation(summary = "取消请假")
    @PostMapping("/cancel/{id}")
    public Result<Boolean> cancelLeave(
            @PathVariable Long id,
            @RequestHeader("userId") Long userId) {
        return Result.success(staffLeaveService.cancelLeave(id, userId));
    }

    @Operation(summary = "查询人员在某时间段的请假")
    @GetMapping("/user/{userId}")
    public Result<List<StaffLeave>> getLeavesByUserAndRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(staffLeaveService.getLeavesByUserAndRange(userId, startDate, endDate));
    }

    @Operation(summary = "获取某时间段休假人员ID列表")
    @GetMapping("/on-leave")
    public Result<List<Long>> getUsersOnLeave(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(staffLeaveService.getUsersOnLeave(startDate, endDate));
    }

    @Operation(summary = "检查某时刻用户是否休假")
    @GetMapping("/check/{userId}")
    public Result<Boolean> isUserOnLeave(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
        return Result.success(staffLeaveService.isUserOnLeave(userId, dateTime));
    }
}
