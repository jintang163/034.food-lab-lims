package com.foodlab.audit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.audit.dto.AuditQueryDTO;
import com.foodlab.audit.dto.AuditSubmitDTO;
import com.foodlab.audit.dto.RetestAdoptDTO;
import com.foodlab.audit.dto.RetestResultSubmitDTO;
import com.foodlab.audit.dto.RetestSubmitDTO;
import com.foodlab.audit.dto.SamplingReviewDTO;
import com.foodlab.audit.entity.AuditRecord;
import com.foodlab.audit.entity.SamplingReview;
import com.foodlab.audit.service.AuditService;
import com.foodlab.audit.service.RetestService;
import com.foodlab.audit.vo.AuditFlowVO;
import com.foodlab.audit.vo.AuditRecordVO;
import com.foodlab.audit.vo.RetestCompareVO;
import com.foodlab.audit.vo.RetestRecordVO;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "审核管理", description = "两级审核流程管理")
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private RetestService retestService;

    @Operation(summary = "提交审核")
    @PostMapping("/submit")
    public Result<Void> submitAudit(@RequestBody AuditSubmitDTO dto,
                                    @RequestHeader("userId") Long userId,
                                    @RequestHeader("userName") String userName) {
        auditService.submitAudit(dto, userId, userName);
        return Result.success();
    }

    @Operation(summary = "发起审核")
    @PostMapping("/start")
    public Result<Void> startAudit(@RequestParam String businessType,
                                   @RequestParam Long businessId,
                                   @RequestParam String businessCode,
                                   @RequestHeader("userId") Long userId) {
        auditService.startAudit(businessType, businessId, businessCode, userId);
        return Result.success();
    }

    @Operation(summary = "获取审核详情")
    @GetMapping("/{id}")
    public Result<AuditRecordVO> getAuditDetail(@PathVariable Long id) {
        return Result.success(auditService.getAuditDetail(id));
    }

    @Operation(summary = "获取业务审核历史")
    @GetMapping("/history")
    public Result<List<AuditRecordVO>> getAuditHistory(
            @RequestParam String businessType,
            @RequestParam Long businessId) {
        return Result.success(auditService.getAuditHistory(businessType, businessId));
    }

    @Operation(summary = "分页查询审核记录")
    @GetMapping("/page")
    public Result<PageResult<AuditRecord>> getAuditPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            AuditQueryDTO queryDTO) {
        IPage<AuditRecord> page = auditService.getAuditPage(pageNum, pageSize, queryDTO);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "获取我的待审核列表")
    @GetMapping("/my-pending")
    public Result<List<AuditRecordVO>> getMyPendingAudits(@RequestHeader("userId") Long userId) {
        return Result.success(auditService.getMyPendingAudits(userId));
    }

    @Operation(summary = "启动审批流程")
    @PostMapping("/process/start")
    public Result<String> startProcess(@RequestParam Long taskId,
                                       @RequestHeader("userId") Long submitterId,
                                       @RequestHeader("userName") String submitterName) {
        String processInstanceId = auditService.startProcess(taskId, submitterId, submitterName);
        return Result.success(processInstanceId);
    }

    @Operation(summary = "完成审核任务")
    @PostMapping("/process/complete")
    public Result<Void> completeTask(@RequestParam String taskId,
                                     @RequestParam String result,
                                     @RequestParam(required = false) String comment) {
        auditService.completeTask(taskId, result, comment);
        return Result.success();
    }

    @Operation(summary = "获取我的待审任务")
    @GetMapping("/my-tasks")
    public Result<List<Map<String, Object>>> getMyAuditTasks(@RequestHeader("userId") Long userId) {
        List<Map<String, Object>> tasks = auditService.getMyAuditTasks(userId);
        return Result.success(tasks);
    }

    @Operation(summary = "获取流程历史")
    @GetMapping("/process/history/{processInstanceId}")
    public Result<List<Map<String, Object>>> getProcessHistory(@PathVariable String processInstanceId) {
        List<Map<String, Object>> history = auditService.getProcessHistory(processInstanceId);
        return Result.success(history);
    }

    @Operation(summary = "获取审核流程图数据")
    @GetMapping("/flow/{processInstanceId}")
    public Result<AuditFlowVO> getAuditFlow(@PathVariable String processInstanceId) {
        return Result.success(auditService.getAuditFlow(processInstanceId));
    }

    @Operation(summary = "驳回至检测人员修改")
    @PostMapping("/reject-to-submitter")
    public Result<Void> rejectToSubmitter(@RequestParam String flowTaskId,
                                          @RequestParam String reason,
                                          @RequestHeader("userId") Long userId,
                                          @RequestHeader("userName") String userName) {
        auditService.rejectToSubmitter(flowTaskId, reason, userId, userName);
        return Result.success();
    }

    @Operation(summary = "发起复测")
    @PostMapping("/trigger-retest")
    public Result<Void> triggerRetest(@RequestParam String flowTaskId,
                                      @RequestParam Long retesterId,
                                      @RequestParam String reason,
                                      @RequestHeader("userId") Long userId,
                                      @RequestHeader("userName") String userName) {
        auditService.triggerRetest(flowTaskId, retesterId, reason, userId, userName);
        return Result.success();
    }

    @Operation(summary = "创建复测记录")
    @PostMapping("/retest/start")
    public Result<RetestRecordVO> startRetest(@RequestBody RetestSubmitDTO dto,
                                              @RequestHeader("userId") Long userId,
                                              @RequestHeader("userName") String userName) {
        return Result.success(retestService.startRetest(dto, userId, userName));
    }

    @Operation(summary = "提交复测结果")
    @PostMapping("/retest/submit-result")
    public Result<RetestRecordVO> submitRetestResult(@RequestBody RetestResultSubmitDTO dto,
                                                     @RequestHeader("userId") Long userId,
                                                     @RequestHeader("userName") String userName) {
        return Result.success(retestService.submitRetestResult(dto, userId, userName));
    }

    @Operation(summary = "对比复测结果")
    @GetMapping("/retest/compare/{retestId}")
    public Result<RetestCompareVO> compareRetestResult(@PathVariable Long retestId) {
        return Result.success(retestService.compareRetestResult(retestId));
    }

    @Operation(summary = "选择采用结果")
    @PostMapping("/retest/adopt")
    public Result<RetestRecordVO> adoptResult(@RequestBody RetestAdoptDTO dto,
                                              @RequestHeader("userId") Long userId,
                                              @RequestHeader("userName") String userName) {
        return Result.success(retestService.adoptResult(dto, userId, userName));
    }

    @Operation(summary = "获取任务复测记录")
    @GetMapping("/retest/task/{taskId}")
    public Result<List<RetestRecordVO>> getRetestByTaskId(@PathVariable Long taskId) {
        return Result.success(retestService.getRetestByTaskId(taskId));
    }

    @Operation(summary = "获取复测详情")
    @GetMapping("/retest/{retestId}")
    public Result<RetestRecordVO> getRetestDetail(@PathVariable Long retestId) {
        return Result.success(retestService.getRetestDetail(retestId));
    }

    @Operation(summary = "创建随机抽样复审")
    @PostMapping("/sampling-review/create")
    public Result<SamplingReview> createSamplingReview(@RequestBody SamplingReviewDTO dto,
                                                       @RequestHeader("userId") Long userId,
                                                       @RequestHeader("userName") String userName) {
        return Result.success(auditService.createSamplingReview(dto, userId, userName));
    }

    @Operation(summary = "获取待抽样复审列表")
    @GetMapping("/sampling-review/pending")
    public Result<List<SamplingReview>> getPendingSamplingReviews() {
        return Result.success(auditService.getPendingSamplingReviews());
    }

    @Operation(summary = "完成抽样复审")
    @PostMapping("/sampling-review/complete")
    public Result<Void> completeSamplingReview(@RequestParam Long reviewId,
                                               @RequestParam String result,
                                               @RequestParam(required = false) String opinion,
                                               @RequestHeader("userId") Long userId,
                                               @RequestHeader("userName") String userName) {
        auditService.completeSamplingReview(reviewId, result, opinion, userId, userName);
        return Result.success();
    }
}
