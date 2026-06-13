package com.foodlab.ncr.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodlab.common.constant.DetectConstants;
import com.foodlab.common.constant.NcrConstants;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.common.result.ResultCode;
import com.foodlab.common.utils.CodeGenerator;
import com.foodlab.detect.entity.DetectResult;
import com.foodlab.detect.mapper.DetectResultMapper;
import com.foodlab.ncr.dto.*;
import com.foodlab.ncr.entity.*;
import com.foodlab.ncr.mapper.*;
import com.foodlab.ncr.service.NcrService;
import com.foodlab.ncr.vo.*;
import com.foodlab.sample.entity.Sample;
import com.foodlab.sample.mapper.SampleMapper;
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
public class NcrServiceImpl extends ServiceImpl<NcrRecordMapper, NcrRecord> implements NcrService {

    private final NcrRecordMapper ncrRecordMapper;
    private final NcrRecheckMapper ncrRecheckMapper;
    private final NcrCauseAnalysisMapper ncrCauseAnalysisMapper;
    private final NcrCorrectiveActionMapper ncrCorrectiveActionMapper;
    private final NcrPreventiveActionMapper ncrPreventiveActionMapper;
    private final DetectResultMapper detectResultMapper;
    private final SampleMapper sampleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NcrRecord createNcr(NcrCreateDTO dto, Long userId) {
        NcrRecord existRecord = ncrRecordMapper.selectByDetectResultId(dto.getDetectResultId());
        if (existRecord != null) {
            throw new BusinessException(ResultCode.NCR_ALREADY_EXIST);
        }

        NcrRecord ncr = new NcrRecord();
        ncr.setNcrCode(CodeGenerator.generateNcrCode());
        ncr.setNcrSource(dto.getNcrSource() != null ? dto.getNcrSource() : NcrConstants.NCR_SOURCE_MANUAL);
        ncr.setSampleId(dto.getSampleId());
        ncr.setSampleCode(dto.getSampleCode());
        ncr.setSampleName(dto.getSampleName());
        ncr.setDetectResultId(dto.getDetectResultId());
        ncr.setDetectItemId(dto.getDetectItemId());
        ncr.setDetectItemName(dto.getDetectItemName());
        ncr.setUnqualifiedDescription(dto.getUnqualifiedDescription());
        ncr.setNcrStatus(NcrConstants.NCR_STATUS_RECHECK);
        ncr.setCurrentStage(NcrConstants.NCR_STATUS_RECHECK);
        ncr.setRemark(dto.getRemark());
        ncr.setAttachFiles(dto.getAttachFiles());
        ncr.setCreateBy(userId);
        ncr.setUpdateBy(userId);

        save(ncr);
        log.info("创建不合格品记录成功，NCR编号：{}", ncr.getNcrCode());
        return ncr;
    }

    @Override
    public NcrDetailVO getNcrDetail(Long ncrId) {
        NcrRecord ncr = getById(ncrId);
        if (ncr == null) {
            throw new BusinessException(ResultCode.NCR_NOT_FOUND);
        }

        NcrDetailVO vo = BeanUtil.copyProperties(ncr, NcrDetailVO.class);

        List<NcrRecheck> recheckList = ncrRecheckMapper.selectByNcrId(ncrId);
        List<NcrRecheckVO> recheckVOS = recheckList.stream()
                .map(r -> BeanUtil.copyProperties(r, NcrRecheckVO.class))
                .collect(Collectors.toList());
        vo.setRecheckList(recheckVOS);

        NcrCauseAnalysis causeAnalysis = ncrCauseAnalysisMapper.selectLatestByNcrId(ncrId);
        if (causeAnalysis != null) {
            vo.setCauseAnalysis(BeanUtil.copyProperties(causeAnalysis, NcrCauseAnalysisVO.class));
        }

        List<NcrCorrectiveAction> correctiveActions = ncrCorrectiveActionMapper.selectByNcrId(ncrId);
        List<NcrCorrectiveActionVO> correctiveActionVOS = correctiveActions.stream()
                .map(a -> BeanUtil.copyProperties(a, NcrCorrectiveActionVO.class))
                .collect(Collectors.toList());
        vo.setCorrectiveActionList(correctiveActionVOS);

        List<NcrPreventiveAction> preventiveActions = ncrPreventiveActionMapper.selectByNcrId(ncrId);
        List<NcrPreventiveActionVO> preventiveActionVOS = preventiveActions.stream()
                .map(a -> BeanUtil.copyProperties(a, NcrPreventiveActionVO.class))
                .collect(Collectors.toList());
        vo.setPreventiveActionList(preventiveActionVOS);

        return vo;
    }

    @Override
    public IPage<NcrRecord> getNcrPage(NcrQueryDTO dto) {
        Page<NcrRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        return ncrRecordMapper.selectNcrPage(page,
                dto.getSampleCode(),
                dto.getDetectItemName(),
                dto.getNcrStatus(),
                dto.getNcrSource(),
                dto.getStartDate(),
                dto.getEndDate());
    }

    @Override
    public List<NcrRecord> getNcrListBySampleId(Long sampleId) {
        return ncrRecordMapper.selectBySampleId(sampleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitRecheck(NcrRecheckSubmitDTO dto, Long userId) {
        NcrRecord ncr = getById(dto.getNcrId());
        if (ncr == null) {
            throw new BusinessException(ResultCode.NCR_NOT_FOUND);
        }
        if (!NcrConstants.NCR_STATUS_RECHECK.equals(ncr.getNcrStatus())) {
            throw new BusinessException(ResultCode.NCR_STATUS_ERROR, "当前状态不允许提交复检结果");
        }

        Integer maxCount = ncrRecheckMapper.getMaxRecheckCount(dto.getNcrId());
        int recheckCount = maxCount != null ? maxCount : 0;
        recheckCount++;

        DetectResult originalResult = detectResultMapper.selectById(ncr.getDetectResultId());

        NcrRecheck recheck = new NcrRecheck();
        recheck.setNcrId(dto.getNcrId());
        recheck.setNcrCode(ncr.getNcrCode());
        recheck.setRecheckCount(recheckCount);
        recheck.setTaskId(dto.getTaskId());
        recheck.setSampleId(ncr.getSampleId());
        recheck.setSampleCode(ncr.getSampleCode());
        recheck.setDetectItemId(ncr.getDetectItemId());
        recheck.setDetectItemName(ncr.getDetectItemName());
        if (originalResult != null) {
            recheck.setDetectMethod(originalResult.getDetectMethod());
            recheck.setDetectStandard(originalResult.getDetectStandard());
            recheck.setLimitType(originalResult.getLimitType());
            recheck.setLimitValueMin(originalResult.getLimitValueMin());
            recheck.setLimitValueMax(originalResult.getLimitValueMax());
        }
        recheck.setInstrument(dto.getInstrument());
        recheck.setDetectTime(dto.getDetectTime() != null ? dto.getDetectTime() : LocalDateTime.now());
        recheck.setDetectPersonId(userId);
        recheck.setResultType(dto.getResultType());
        recheck.setResultValue(dto.getResultValue());
        recheck.setResultUnit(dto.getResultUnit());
        recheck.setQualitativeResult(dto.getQualitativeResult());
        recheck.setFinalJudge(dto.getFinalJudge());
        recheck.setRecheckRemark(dto.getRecheckRemark());
        recheck.setAttachFiles(dto.getAttachFiles());
        recheck.setCreateBy(userId);
        recheck.setUpdateBy(userId);

        String recheckStatus = DetectConstants.JUDGE_QUALIFIED.equals(dto.getFinalJudge())
                ? NcrConstants.RECHECK_STATUS_QUALIFIED
                : NcrConstants.RECHECK_STATUS_UNQUALIFIED;
        recheck.setRecheckStatus(recheckStatus);

        ncrRecheckMapper.insert(recheck);

        if (DetectConstants.JUDGE_QUALIFIED.equals(dto.getFinalJudge())) {
            ncr.setNcrStatus(NcrConstants.NCR_STATUS_CLOSED);
            ncr.setCurrentStage(NcrConstants.NCR_STATUS_CLOSED);
            ncr.setCloseTime(LocalDateTime.now());
            ncr.setClosePersonId(userId);
            ncr.setCloseRemark("复检合格，关闭不合格品流程");
            log.info("复检合格，关闭不合格品流程，NCR编号：{}", ncr.getNcrCode());
        } else {
            ncr.setNcrStatus(NcrConstants.NCR_STATUS_CAUSE_ANALYSIS);
            ncr.setCurrentStage(NcrConstants.NCR_STATUS_CAUSE_ANALYSIS);
            ncr.setRecheckTime(LocalDateTime.now());
        }
        ncr.setUpdateBy(userId);
        updateById(ncr);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitCauseAnalysis(NcrCauseAnalysisSubmitDTO dto, Long userId) {
        NcrRecord ncr = getById(dto.getNcrId());
        if (ncr == null) {
            throw new BusinessException(ResultCode.NCR_NOT_FOUND);
        }
        if (!NcrConstants.NCR_STATUS_CAUSE_ANALYSIS.equals(ncr.getNcrStatus())) {
            throw new BusinessException(ResultCode.NCR_STATUS_ERROR, "当前状态不允许提交原因分析");
        }

        NcrCauseAnalysis causeAnalysis = new NcrCauseAnalysis();
        causeAnalysis.setNcrId(dto.getNcrId());
        causeAnalysis.setNcrCode(ncr.getNcrCode());
        causeAnalysis.setCauseType(dto.getCauseType());
        causeAnalysis.setCauseDescription(dto.getCauseDescription());
        causeAnalysis.setRootCause(dto.getRootCause());
        causeAnalysis.setImpactAnalysis(dto.getImpactAnalysis());
        causeAnalysis.setAnalysisPersonId(dto.getAnalysisPersonId() != null ? dto.getAnalysisPersonId() : userId);
        causeAnalysis.setAnalysisPersonName(dto.getAnalysisPersonName());
        causeAnalysis.setAnalysisTime(dto.getAnalysisTime() != null ? dto.getAnalysisTime() : LocalDateTime.now());
        causeAnalysis.setRemark(dto.getRemark());
        causeAnalysis.setAttachFiles(dto.getAttachFiles());
        causeAnalysis.setCreateBy(userId);
        causeAnalysis.setUpdateBy(userId);

        ncrCauseAnalysisMapper.insert(causeAnalysis);

        ncr.setNcrStatus(NcrConstants.NCR_STATUS_CORRECTIVE_ACTION);
        ncr.setCurrentStage(NcrConstants.NCR_STATUS_CORRECTIVE_ACTION);
        ncr.setCauseAnalysisTime(LocalDateTime.now());
        ncr.setUpdateBy(userId);
        updateById(ncr);

        log.info("提交原因分析成功，NCR编号：{}", ncr.getNcrCode());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitCorrectiveAction(NcrActionSubmitDTO dto, Long userId) {
        NcrRecord ncr = getById(dto.getNcrId());
        if (ncr == null) {
            throw new BusinessException(ResultCode.NCR_NOT_FOUND);
        }
        if (!NcrConstants.NCR_STATUS_CORRECTIVE_ACTION.equals(ncr.getNcrStatus())) {
            throw new BusinessException(ResultCode.NCR_STATUS_ERROR, "当前状态不允许提交纠正措施");
        }

        NcrCorrectiveAction action = new NcrCorrectiveAction();
        action.setNcrId(dto.getNcrId());
        action.setNcrCode(ncr.getNcrCode());
        action.setActionDescription(dto.getActionDescription());
        action.setActionPlan(dto.getActionPlan());
        action.setActionPersonId(dto.getActionPersonId());
        action.setActionPersonName(dto.getActionPersonName());
        action.setPlanStartTime(dto.getPlanStartTime());
        action.setPlanEndTime(dto.getPlanEndTime());
        action.setActualStartTime(dto.getActualStartTime());
        action.setActualEndTime(dto.getActualEndTime());
        action.setActionStatus(dto.getActionStatus() != null ? dto.getActionStatus() : NcrConstants.ACTION_STATUS_COMPLETED);
        action.setActionResult(dto.getActionResult());
        action.setEffectivenessEvaluation(dto.getEffectivenessEvaluation());
        action.setVerifyPersonId(dto.getVerifyPersonId());
        action.setVerifyPersonName(dto.getVerifyPersonName());
        action.setVerifyTime(dto.getVerifyTime());
        action.setVerifyOpinion(dto.getVerifyOpinion());
        action.setRemark(dto.getRemark());
        action.setAttachFiles(dto.getAttachFiles());
        action.setCreateBy(userId);
        action.setUpdateBy(userId);

        ncrCorrectiveActionMapper.insert(action);

        ncr.setNcrStatus(NcrConstants.NCR_STATUS_PREVENTIVE_ACTION);
        ncr.setCurrentStage(NcrConstants.NCR_STATUS_PREVENTIVE_ACTION);
        ncr.setCorrectiveActionTime(LocalDateTime.now());
        ncr.setUpdateBy(userId);
        updateById(ncr);

        log.info("提交纠正措施成功，NCR编号：{}", ncr.getNcrCode());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitPreventiveAction(NcrActionSubmitDTO dto, Long userId) {
        NcrRecord ncr = getById(dto.getNcrId());
        if (ncr == null) {
            throw new BusinessException(ResultCode.NCR_NOT_FOUND);
        }
        if (!NcrConstants.NCR_STATUS_PREVENTIVE_ACTION.equals(ncr.getNcrStatus())) {
            throw new BusinessException(ResultCode.NCR_STATUS_ERROR, "当前状态不允许提交预防措施");
        }

        NcrPreventiveAction action = new NcrPreventiveAction();
        action.setNcrId(dto.getNcrId());
        action.setNcrCode(ncr.getNcrCode());
        action.setActionDescription(dto.getActionDescription());
        action.setActionPlan(dto.getActionPlan());
        action.setActionPersonId(dto.getActionPersonId());
        action.setActionPersonName(dto.getActionPersonName());
        action.setPlanStartTime(dto.getPlanStartTime());
        action.setPlanEndTime(dto.getPlanEndTime());
        action.setActualStartTime(dto.getActualStartTime());
        action.setActualEndTime(dto.getActualEndTime());
        action.setActionStatus(dto.getActionStatus() != null ? dto.getActionStatus() : NcrConstants.ACTION_STATUS_COMPLETED);
        action.setActionResult(dto.getActionResult());
        action.setEffectivenessEvaluation(dto.getEffectivenessEvaluation());
        action.setVerifyPersonId(dto.getVerifyPersonId());
        action.setVerifyPersonName(dto.getVerifyPersonName());
        action.setVerifyTime(dto.getVerifyTime());
        action.setVerifyOpinion(dto.getVerifyOpinion());
        action.setRemark(dto.getRemark());
        action.setAttachFiles(dto.getAttachFiles());
        action.setCreateBy(userId);
        action.setUpdateBy(userId);

        ncrPreventiveActionMapper.insert(action);

        ncr.setPreventiveActionTime(LocalDateTime.now());
        ncr.setUpdateBy(userId);
        updateById(ncr);

        log.info("提交预防措施成功，NCR编号：{}", ncr.getNcrCode());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeNcr(NcrCloseDTO dto, Long userId) {
        NcrRecord ncr = getById(dto.getNcrId());
        if (ncr == null) {
            throw new BusinessException(ResultCode.NCR_NOT_FOUND);
        }
        if (NcrConstants.NCR_STATUS_CLOSED.equals(ncr.getNcrStatus())
                || NcrConstants.NCR_STATUS_CANCELLED.equals(ncr.getNcrStatus())) {
            throw new BusinessException(ResultCode.NCR_STATUS_ERROR, "不合格品已关闭或取消，无法重复关闭");
        }

        ncr.setNcrStatus(NcrConstants.NCR_STATUS_CLOSED);
        ncr.setCurrentStage(NcrConstants.NCR_STATUS_CLOSED);
        ncr.setCloseTime(LocalDateTime.now());
        ncr.setClosePersonId(userId);
        ncr.setCloseRemark(dto.getCloseRemark());
        ncr.setUpdateBy(userId);
        updateById(ncr);

        log.info("关闭不合格品流程，NCR编号：{}", ncr.getNcrCode());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelNcr(Long ncrId, Long userId, String remark) {
        NcrRecord ncr = getById(ncrId);
        if (ncr == null) {
            throw new BusinessException(ResultCode.NCR_NOT_FOUND);
        }
        if (NcrConstants.NCR_STATUS_CLOSED.equals(ncr.getNcrStatus())
                || NcrConstants.NCR_STATUS_CANCELLED.equals(ncr.getNcrStatus())) {
            throw new BusinessException(ResultCode.NCR_STATUS_ERROR, "不合格品已关闭或取消，无法重复操作");
        }

        ncr.setNcrStatus(NcrConstants.NCR_STATUS_CANCELLED);
        ncr.setCurrentStage(NcrConstants.NCR_STATUS_CANCELLED);
        ncr.setCloseTime(LocalDateTime.now());
        ncr.setClosePersonId(userId);
        ncr.setCloseRemark(remark);
        ncr.setUpdateBy(userId);
        updateById(ncr);

        log.info("取消不合格品流程，NCR编号：{}", ncr.getNcrCode());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NcrRecord autoCreateNcrFromDetectResult(Long detectResultId, Long userId) {
        DetectResult detectResult = detectResultMapper.selectById(detectResultId);
        if (detectResult == null) {
            throw new BusinessException(ResultCode.RESULT_NOT_FOUND);
        }

        if (!DetectConstants.JUDGE_UNQUALIFIED.equals(detectResult.getFinalJudge())) {
            return null;
        }

        NcrRecord existRecord = ncrRecordMapper.selectByDetectResultId(detectResultId);
        if (existRecord != null) {
            log.info("检测结果已存在不合格品记录，检测结果ID：{}", detectResultId);
            return existRecord;
        }

        Sample sample = sampleMapper.selectById(detectResult.getSampleId());

        NcrCreateDTO createDTO = new NcrCreateDTO();
        createDTO.setSampleId(detectResult.getSampleId());
        createDTO.setSampleCode(detectResult.getSampleCode());
        if (sample != null) {
            createDTO.setSampleName(sample.getSampleName());
        }
        createDTO.setDetectResultId(detectResultId);
        createDTO.setDetectItemId(detectResult.getDetectItemId());
        createDTO.setDetectItemName(detectResult.getDetectItemName());
        createDTO.setUnqualifiedDescription("检测结果不合格，自动启动不合格品处置流程");
        createDTO.setNcrSource(NcrConstants.NCR_SOURCE_AUTO);

        return createNcr(createDTO, userId);
    }
}
