package com.foodlab.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodlab.audit.dto.AuditQueryDTO;
import com.foodlab.audit.dto.AuditSubmitDTO;
import com.foodlab.audit.dto.SamplingReviewDTO;
import com.foodlab.audit.entity.AuditRecord;
import com.foodlab.audit.entity.SamplingReview;
import com.foodlab.audit.vo.AuditFlowVO;
import com.foodlab.audit.vo.AuditRecordVO;

import java.util.List;
import java.util.Map;

public interface AuditService extends IService<AuditRecord> {

    boolean submitAudit(AuditSubmitDTO dto, Long userId, String userName);

    boolean startAudit(String businessType, Long businessId, String businessCode, Long creatorId);

    AuditRecordVO getAuditDetail(Long id);

    List<AuditRecordVO> getAuditHistory(String businessType, Long businessId);

    IPage<AuditRecord> getAuditPage(int pageNum, int pageSize, AuditQueryDTO queryDTO);

    List<AuditRecordVO> getMyPendingAudits(Long auditorId);

    String startProcess(Long taskId, Long submitterId, String submitterName);

    void completeTask(String taskId, String result, String comment);

    List<Map<String, Object>> getMyAuditTasks(Long userId);

    List<Map<String, Object>> getProcessHistory(String processInstanceId);

    AuditFlowVO getAuditFlow(String processInstanceId);

    void rejectToSubmitter(String flowTaskId, String reason, Long userId, String userName);

    void triggerRetest(String flowTaskId, Long retesterId, String reason, Long userId, String userName);

    SamplingReview createSamplingReview(SamplingReviewDTO dto, Long userId, String userName);

    List<SamplingReview> getPendingSamplingReviews();

    void completeSamplingReview(Long reviewId, String result, String opinion, Long userId, String userName);
}
