package com.foodlab.task.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import com.foodlab.task.dto.TaskAssignDTO;
import com.foodlab.task.dto.TaskQueryDTO;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.service.DetectTaskService;
import com.foodlab.task.vo.TaskDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "检测任务管理")
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final DetectTaskService detectTaskService;

    @Operation(summary = "分配任务")
    @PostMapping("/assign")
    public Result<DetectTask> assignTask(@Valid @RequestBody TaskAssignDTO dto,
                                         @RequestHeader("userId") Long userId,
                                         @RequestHeader("userName") String userName) {
        DetectTask task = detectTaskService.assignTask(dto, userId, userName);
        return Result.success(task);
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public Result<TaskDetailVO> getTaskDetail(@PathVariable Long id) {
        TaskDetailVO detail = detectTaskService.getTaskDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "分页查询任务列表")
    @GetMapping("/page")
    public Result<PageResult<DetectTask>> getTaskPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            TaskQueryDTO queryDTO) {
        IPage<DetectTask> page = detectTaskService.getTaskPage(pageNum, pageSize, queryDTO);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "开始任务")
    @PostMapping("/start/{taskId}")
    public Result<Void> startTask(@PathVariable Long taskId,
                                  @RequestHeader("userId") Long userId) {
        detectTaskService.startTask(taskId, userId);
        return Result.success();
    }

    @Operation(summary = "完成检测，提交审核")
    @PostMapping("/complete/{taskId}")
    public Result<Void> completeTask(@PathVariable Long taskId,
                                     @RequestHeader("userId") Long userId) {
        detectTaskService.completeTask(taskId, userId);
        return Result.success();
    }

    @Operation(summary = "提交审核")
    @PostMapping("/submit/{taskId}")
    public Result<Void> submitTask(@PathVariable Long taskId,
                                   @RequestHeader("userId") Long userId) {
        detectTaskService.submitTask(taskId, userId);
        return Result.success();
    }

    @Operation(summary = "取消任务")
    @PostMapping("/cancel/{taskId}")
    public Result<Void> cancelTask(@PathVariable Long taskId,
                                   @RequestHeader("userId") Long userId) {
        detectTaskService.cancelTask(taskId, userId);
        return Result.success();
    }

    @Operation(summary = "获取我的任务列表")
    @GetMapping("/my-tasks")
    public Result<List<DetectTask>> getMyTasks(
            @RequestHeader("userId") Long userId,
            @RequestParam(required = false) String status) {
        return Result.success(detectTaskService.getMyTasks(userId, status));
    }