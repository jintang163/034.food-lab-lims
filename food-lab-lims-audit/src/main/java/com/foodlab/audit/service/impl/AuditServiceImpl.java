package com.foodlab.audit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodlab.audit.dto.AuditQueryDTO;
import com.foodlab.audit.dto.AuditSubmitDTO;
import com.foodlab.audit.entity.AuditRecord;
import com.foodlab.audit.mapper.AuditRecordMapper;
import com.foodlab.audit.service.AuditService;
import com.foodlab.audit.vo.AuditRecordVO;
import com.foodlab.common.constant.AuditConstants;
import com.foodlab.common.constant.DetectConstants;
import com.foodlab.common.constant.TaskConstants;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.common.result.ResultCode;
import com.foodlab.common.utils.CodeGenerator;
import com.foodlab.detect.entity.DetectResult;
import com.foodlab.detect.mapper.DetectResultMapper;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.mapper.DetectTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl extends ServiceImpl<AuditRecordMapper, AuditRecord> implements AuditService {

    private final AuditRecordMapper auditRecordMapper;
    private final DetectTaskMapper detectTaskMapper;
    private final DetectResultMapper detectResultMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitAudit(AuditSubmitDTO dto, Long userId, String userName) {
        AuditRecord currentRecord = auditRecordMapper.selectLatestByBusinessAndLevel(
                dto.getBusinessType(), dto.getBusinessId(), dto.getAuditLevel());

        if (currentRecord == null) {
            throw new BusinessException(ResultCode.AUDIT_NOT_FOUND);
        }

        if (!AuditConstants.AUDIT_STATUS_PENDING.equals(currentRecord.getAuditStatus())) {
            throw new BusinessException(ResultCode.AUDIT_STATUS_ERROR, "当前审核状态不允许操作");
        }

        boolean isPass = AuditConstants.AUDIT_RESULT_PASS.equals(dto.getAuditResult());

        currentRecord.setAuditStatus(isPass ? AuditConstants.AUDIT_STATUS_PASS : AuditConstants.AUDIT_STATUS_REJECT);
        currentRecord.setAuditorId(userId);
        currentRecord.setAuditorName(userName);
        currentRecord.setAuditTime(LocalDateTime.now());
        currentRecord.setAuditOpinion(dto.getAuditOpinion());
        currentRecord.setRemark(dto.getRemark());
        currentRecord.setUpdateBy(userId);

        updateById(currentRecord);

        if (isPass) {
            if (dto.getAuditLevel() == 1) {
                createSecondLevelAudit(dto, currentRecord.getId(), userId);
                updateBusinessStatus(dto.getBusinessType(), dto.getBusinessId(), TaskConstants.TASK_STATUS_SECOND_AUDIT);
            } else {
                updateBusinessStatus(dto.getBusinessType(), dto.getBusinessId(), TaskConstants.TASK_STATUS_APPROVED);
                updateDetectResultsAuditStatus(dto.getBusinessId(), DetectConstants.IS_AUDIT_YES);
            }
        } else {
            updateBusinessStatus(dto.getBusinessType(), dto.getBusinessId(), TaskConstants.TASK_STATUS_REJECTED);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startAudit(String businessType, Long businessId, String businessCode, Long creatorId) {
        List<AuditRecord> existing = auditRecordMapper.selectByBusiness(businessType, businessId);
        if (!existing.isEmpty()) {
            existing.forEach(ar -> {
                ar.setDeleted(1);
                updateById(ar);
            });
        }

        AuditRecord firstAudit = new AuditRecord();
        firstAudit.setAuditCode(CodeGenerator.generateAuditCode());
        firstAudit.setBusinessType(businessType);
        firstAudit.setBusinessId(businessId);
        firstAudit.setBusinessCode(businessCode);
        firstAudit.setAuditLevel(1);
        firstAudit.setAuditStatus(AuditConstants.AUDIT_STATUS_PENDING);
        firstAudit.setCreateBy(creatorId);
        firstAudit.setUpdateBy(creatorId);
        save(firstAudit);

        updateBusinessStatus(businessType, businessId, TaskConstants.TASK_STATUS_FIRST_AUDIT);

        return true;
    }

    @Override
    public AuditRecordVO getAuditDetail(Long id) {
        AuditRecord record = getById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.AUDIT_NOT_FOUND);
        }
        return BeanUtil.copyProperties(record, AuditRecordVO.class);
    }

    @Override
    public List<AuditRecordVO> getAuditHistory(String businessType, Long businessId) {
        List<AuditRecord> records = auditRecordMapper.selectByBusiness(businessType, businessId);
        return records.stream()
                .map(r -> BeanUtil.copyProperties(r, AuditRecordVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public IPage<AuditRecord> getAuditPage(int pageNum, int pageSize, AuditQueryDTO queryDTO) {
        Page<AuditRecord> page = new Page<>(pageNum, pageSize);
        return auditRecordMapper.selectAuditPage(page,
                queryDTO.getBusinessType(),
                queryDTO.getBusinessCode(),
                queryDTO.getAuditLevel(),
                queryDTO.getAuditStatus(),
                queryDTO.getAuditorId(),
                queryDTO.getStartDate(),
                queryDTO.getEndDate());
    }

    @Override
    public List<AuditRecordVO> getMyPendingAudits(Long auditorId) {
        List<AuditRecord> records = auditRecordMapper.selectPendingByAuditorId(auditorId);
        return records.stream()
                .map(r -> BeanUtil.copyProperties(r, AuditRecordVO.class))
                .collect(Collectors.toList());
    }

    private void createSecondLevelAudit(AuditSubmitDTO dto, Long previousAuditId, Long userId) {
        AuditRecord secondAudit = new AuditRecord();
        secondAudit.setAuditCode(CodeGenerator.generateAuditCode());
        secondAudit.setBusinessType(dto.getBusinessType());
        secondAudit.setBusinessId(dto.getBusinessId());
        secondAudit.setBusinessCode(dto.getBusinessCode());
        secondAudit.setAuditLevel(2);
        secondAudit.setAuditStatus(AuditConstants.AUDIT_STATUS_PENDING);
        secondAudit.setPreviousAuditId(previousAuditId);
        secondAudit.setCreateBy(userId);
        secondAudit.setUpdateBy(userId);
        save(secondAudit);

        AuditRecord firstAudit = getById(previousAuditId);
        if (firstAudit != null) {
            firstAudit.setNextAuditId(secondAudit.getId());
            updateById(firstAudit);
        }
    }

    private void updateBusinessStatus(String businessType, Long businessId, String status) {
        if ("task".equals(businessType)) {
            DetectTask task = detectTaskMapper.selectById(businessId);
            if (task != null) {
                task.setTaskStatus(status);
                detectTaskMapper.updateById(task);
            }
        }
    }

    private void updateDetectResultsAuditStatus(Long taskId, String isAudit) {
        List<DetectResult> results = detectResultMapper.selectByTaskId(taskId);
        for (DetectResult result : results) {
            result.setIsAudit(isAudit);
            detectResultMapper.updateById(result);
        }
    }
}
