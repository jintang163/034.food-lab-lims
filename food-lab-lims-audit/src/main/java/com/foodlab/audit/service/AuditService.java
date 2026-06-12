package com.foodlab.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodlab.audit.dto.AuditQueryDTO;
import com.foodlab.audit.dto.AuditSubmitDTO;
import com.foodlab.audit.entity.AuditRecord;
import com.foodlab.audit.vo.AuditRecordVO;

import java.util.List;

public interface AuditService extends IService<AuditRecord> {

    boolean submitAudit(AuditSubmitDTO dto, Long userId, String userName);

    boolean startAudit(String businessType, Long businessId, String businessCode, Long creatorId);

    AuditRecordVO getAuditDetail(Long id);

    List<AuditRecordVO> getAuditHistory(String businessType, Long businessId);

    IPage<AuditRecord> getAuditPage(int pageNum, int pageSize, AuditQueryDTO queryDTO);

    List<AuditRecordVO> getMyPendingAudits(Long auditorId);
}
