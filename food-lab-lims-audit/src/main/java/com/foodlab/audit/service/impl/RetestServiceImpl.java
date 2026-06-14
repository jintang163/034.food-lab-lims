package com.foodlab.audit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodlab.audit.dto.RetestAdoptDTO;
import com.foodlab.audit.dto.RetestResultSubmitDTO;
import com.foodlab.audit.dto.RetestSubmitDTO;
import com.foodlab.audit.entity.AuditRecord;
import com.foodlab.audit.entity.RetestRecord;
import com.foodlab.audit.mapper.AuditRecordMapper;
import com.foodlab.audit.mapper.RetestRecordMapper;
import com.foodlab.audit.service.RetestService;
import com.foodlab.audit.vo.RetestCompareVO;
import com.foodlab.audit.vo.RetestRecordVO;
import com.foodlab.common.constant.AuditConstants;
import com.foodlab.common.constant.TaskConstants;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.common.result.ResultCode;
import com.foodlab.common.utils.CodeGenerator;
import com.foodlab.detect.entity.DetectResult;
import com.foodlab.detect.mapper.DetectResultMapper;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.service.DetectTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetestServiceImpl extends ServiceImpl<RetestRecordMapper, RetestRecord> implements RetestService {

    private final RetestRecordMapper retestRecordMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final DetectResultMapper detectResultMapper;
    private final DetectTaskService detectTaskService;
    private final RuntimeService runtimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RetestRecordVO startRetest(RetestSubmitDTO dto, Long userId, String userName) {
        DetectResult originalResult = detectResultMapper.selectById(dto.getOriginalResultId());
        if (originalResult == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "原检测结果不存在");
        }

        DetectTask task = detectTaskService.getById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }

        RetestRecord retestRecord = new RetestRecord();
        retestRecord.setRetestCode(CodeGenerator.generateRetestCode());
        retestRecord.setOriginalResultId(dto.getOriginalResultId());
        retestRecord.setTaskId(dto.getTaskId());
        retestRecord.setSampleCode(task.getSampleCode());
        retestRecord.setDetectItemName(originalResult.getDetectItemName());
        retestRecord.setTriggerAuditId(dto.getTriggerAuditId());
        retestRecord.setTriggerReason(dto.getTriggerReason());
        retestRecord.setOriginalValue(originalResult.getResultValue());
        retestRecord.setOriginalJudge(originalResult.getJudgeResult());
        retestRecord.setRetestStatus(AuditConstants.RETEST_STATUS_PENDING);
        retestRecord.setRetesterId(dto.getRetesterId());
        retestRecord.setCreateBy(userId);
        retestRecord.setUpdateBy(userId);
        save(retestRecord);

        AuditRecord auditRecord = auditRecordMapper.selectById(dto.getTriggerAuditId());
        if (auditRecord != null) {
            auditRecord.setRetestId(retestRecord.getId());
            auditRecord.setActionType(AuditConstants.ACTION_TYPE_RETEST);
            auditRecord.setUpdateBy(userId);
            auditRecordMapper.updateById(auditRecord);
        }

        task.setTaskStatus(TaskConstants.TASK_STATUS_RETEST);
        detectTaskService.updateById(task);

        Map<String, Object> variables = new HashMap<>();
        variables.put("taskId", dto.getTaskId());
        variables.put("originalResultId", dto.getOriginalResultId());
        variables.put("triggerAuditId", dto.getTriggerAuditId());
        variables.put("triggerReason", dto.getTriggerReason());
        variables.put("retesterId", dto.getRetesterId());
        variables.put("sampleCode", task.getSampleCode());
        variables.put("detectItemName", originalResult.getDetectItemName());

        org.flowable.engine.runtime.ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("retestProcess", String.valueOf(retestRecord.getId()), variables);

        retestRecord.setProcessInstanceId(processInstance.getId());
        retestRecordMapper.updateById(retestRecord);

        log.info("复测流程已启动，复测编号：{}，流程实例ID：{}", retestRecord.getRetestCode(), processInstance.getId());

        return BeanUtil.copyProperties(retestRecord, RetestRecordVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RetestRecordVO submitRetestResult(RetestResultSubmitDTO dto, Long userId, String userName) {
        RetestRecord retestRecord = getById(dto.getRetestId());
        if (retestRecord == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "复测记录不存在");
        }

        if (!AuditConstants.RETEST_STATUS_PENDING.equals(retestRecord.getRetestStatus())
                && !AuditConstants.RETEST_STATUS_DETECTING.equals(retestRecord.getRetestStatus())) {
            throw new BusinessException(ResultCode.AUDIT_STATUS_ERROR, "当前复测状态不允许提交结果");
        }

        retestRecord.setRetestValue(dto.getRetestValue());
        retestRecord.setRetestJudge(dto.getRetestJudge());
        retestRecord.setRetestStatus(AuditConstants.RETEST_STATUS_COMPLETED);
        retestRecord.setRetesterId(userId);
        retestRecord.setRetesterName(userName);
        retestRecord.setRetestTime(LocalDateTime.now());
        retestRecord.setUpdateBy(userId);
        updateById(retestRecord);

        log.info("复测结果已提交，复测编号：{}", retestRecord.getRetestCode());
        return BeanUtil.copyProperties(retestRecord, RetestRecordVO.class);
    }

    @Override
    public RetestCompareVO compareRetestResult(Long retestId) {
        RetestRecord retestRecord = getById(retestId);
        if (retestRecord == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "复测记录不存在");
        }

        RetestCompareVO compareVO = new RetestCompareVO();
        compareVO.setRetestId(retestRecord.getId());
        compareVO.setRetestCode(retestRecord.getRetestCode());
        compareVO.setOriginalValue(retestRecord.getOriginalValue());
        compareVO.setOriginalJudge(retestRecord.getOriginalJudge());
        compareVO.setRetestValue(retestRecord.getRetestValue());
        compareVO.setRetestJudge(retestRecord.getRetestJudge());
        compareVO.setAdoptedResult(retestRecord.getAdoptedResult());
        compareVO.setAdoptOpinion(retestRecord.getAdoptOpinion());

        if (retestRecord.getRetestValue() != null) {
            compareVO.setValueChanged(!retestRecord.getOriginalValue().equals(retestRecord.getRetestValue()));
            compareVO.setJudgeChanged(
                    (retestRecord.getOriginalJudge() == null && retestRecord.getRetestJudge() != null)
                            || (retestRecord.getOriginalJudge() != null && !retestRecord.getOriginalJudge().equals(retestRecord.getRetestJudge()))
            );
        }

        return compareVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RetestRecordVO adoptResult(RetestAdoptDTO dto, Long userId, String userName) {
        RetestRecord retestRecord = getById(dto.getRetestId());
        if (retestRecord == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "复测记录不存在");
        }

        if (!AuditConstants.RETEST_STATUS_COMPLETED.equals(retestRecord.getRetestStatus())) {
            throw new BusinessException(ResultCode.AUDIT_STATUS_ERROR, "复测未完成，无法选择采用");
        }

        retestRecord.setAdoptedResult(dto.getAdoptedResult());
        retestRecord.setAdoptOpinion(dto.getAdoptOpinion());
        retestRecord.setRetestStatus(AuditConstants.RETEST_STATUS_ADOPTED);
        retestRecord.setAdopterId(userId);
        retestRecord.setAdopterName(userName);
        retestRecord.setAdoptTime(LocalDateTime.now());
        retestRecord.setUpdateBy(userId);
        updateById(retestRecord);

        if (dto.getAdoptedResult() != null) {
            DetectResult originalResult = detectResultMapper.selectById(retestRecord.getOriginalResultId());
            if (originalResult != null) {
                if (AuditConstants.ADOPTED_RETEST.equals(dto.getAdoptedResult())) {
                    originalResult.setResultValue(retestRecord.getRetestValue());
                    originalResult.setJudgeResult(retestRecord.getRetestJudge());
                    originalResult.setIsAudit(DetectConstants.IS_AUDIT_NO);
                    detectResultMapper.updateById(originalResult);
                }
            }
        }

        DetectTask task = detectTaskService.getById(retestRecord.getTaskId());
        if (task != null) {
            task.setTaskStatus(TaskConstants.TASK_STATUS_SUBMITTED);
            detectTaskService.updateById(task);
        }

        log.info("复测结果已选择采用，复测编号：{}，采用结果：{}", retestRecord.getRetestCode(), dto.getAdoptedResult());
        return BeanUtil.copyProperties(retestRecord, RetestRecordVO.class);
    }

    @Override
    public List<RetestRecordVO> getRetestByTaskId(Long taskId) {
        List<RetestRecord> records = retestRecordMapper.selectByTaskId(taskId);
        return records.stream()
                .map(r -> BeanUtil.copyProperties(r, RetestRecordVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public RetestRecordVO getRetestDetail(Long retestId) {
        RetestRecord record = getById(retestId);
        if (record == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "复测记录不存在");
        }
        return BeanUtil.copyProperties(record, RetestRecordVO.class);
    }
}
