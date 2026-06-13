package com.foodlab.detect.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodlab.common.constant.DetectConstants;
import com.foodlab.common.constant.TaskConstants;
import com.foodlab.common.domain.OfflineSyncResult;
import com.foodlab.common.event.DetectResultUnqualifiedEvent;
import com.foodlab.common.event.TaskCompletedEvent;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.common.result.ResultCode;
import com.foodlab.common.utils.CodeGenerator;
import com.foodlab.detect.dto.DetectResultSubmitDTO;
import com.foodlab.detect.dto.DetectResultSyncDTO;
import com.foodlab.detect.entity.DetectItem;
import com.foodlab.detect.entity.DetectRawData;
import com.foodlab.detect.entity.DetectResult;
import com.foodlab.detect.entity.LimitStandard;
import com.foodlab.detect.mapper.DetectItemMapper;
import com.foodlab.detect.mapper.DetectRawDataMapper;
import com.foodlab.detect.mapper.DetectResultMapper;
import com.foodlab.detect.mapper.LimitStandardMapper;
import com.foodlab.detect.service.DetectResultService;
import com.foodlab.detect.vo.DetectResultDetailVO;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.mapper.DetectTaskMapper;
import com.foodlab.task.service.DetectTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetectResultServiceImpl extends ServiceImpl<DetectResultMapper, DetectResult> implements DetectResultService {

    private final DetectResultMapper detectResultMapper;
    private final DetectRawDataMapper detectRawDataMapper;
    private final DetectItemMapper detectItemMapper;
    private final LimitStandardMapper limitStandardMapper;
    private final DetectTaskMapper detectTaskMapper;
    private final DetectTaskService detectTaskService;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DetectResult submitResult(DetectResultSubmitDTO dto, Long userId) {
        DetectTask task = detectTaskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        if (!TaskConstants.TASK_STATUS_IN_PROGRESS.equals(task.getTaskStatus())) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR, "任务状态不正确，无法录入结果");
        }

        DetectItem detectItem = detectItemMapper.selectById(dto.getDetectItemId());
        if (detectItem == null) {
            throw new BusinessException(ResultCode.DETECT_ITEM_NOT_FOUND);
        }

        LimitStandard limitStandard = null;
        if (dto.getLimitStandardId() != null) {
            limitStandard = limitStandardMapper.selectById(dto.getLimitStandardId());
        } else {
            List<LimitStandard> standards = limitStandardMapper.selectByDetectItemId(dto.getDetectItemId());
            if (!standards.isEmpty()) {
                limitStandard = standards.get(0);
            }
        }

        String autoJudge = autoJudge(dto.getDetectItemId(), dto);

        DetectResult existResult = detectResultMapper.selectByTaskAndItem(dto.getTaskId(), dto.getDetectItemId());
        DetectResult result;
        if (existResult != null) {
            result = existResult;
            detectRawDataMapper.deleteByResultId(result.getId());
        } else {
            result = new DetectResult();
            result.setResultCode(CodeGenerator.generateResultCode());
            result.setTaskId(dto.getTaskId());
            result.setSampleId(dto.getSampleId());
            result.setSampleCode(dto.getSampleCode());
            result.setDetectItemId(dto.getDetectItemId());
            result.setDetectItemName(detectItem.getItemName());
            result.setCreateBy(userId);
        }

        result.setDetectMethod(detectItem.getDetectMethod());
        result.setDetectStandard(detectItem.getDetectStandard());
        result.setInstrument(dto.getInstrument());
        result.setDetectTime(dto.getDetectTime() != null ? dto.getDetectTime() : LocalDateTime.now());
        result.setDetectPersonId(userId);
        result.setResultType(dto.getResultType());
        result.setResultValue(dto.getResultValue());
        result.setResultUnit(dto.getResultUnit() != null ? dto.getResultUnit() : detectItem.getUnit());
        result.setQualitativeResult(dto.getQualitativeResult());
        result.setCalculateFormula(dto.getCalculateFormula());
        result.setRemark(dto.getRemark());
        result.setAttachFiles(dto.getAttachFiles());
        result.setAutoJudge(autoJudge);
        result.setFinalJudge(autoJudge);
        result.setIsAudit("0");
        result.setUpdateBy(userId);

        if (limitStandard != null) {
            result.setLimitType(limitStandard.getLimitType());
            result.setLimitValueMin(limitStandard.getLimitValueMin());
            result.setLimitValueMax(limitStandard.getLimitValueMax());
        }

        saveOrUpdate(result);

        if (DetectConstants.JUDGE_UNQUALIFIED.equals(autoJudge)) {
            eventPublisher.publishEvent(new DetectResultUnqualifiedEvent(
                    this,
                    result.getId(),
                    result.getSampleId(),
                    result.getSampleCode(),
                    result.getDetectItemId(),
                    result.getDetectItemName(),
                    dto.getTaskId(),
                    userId,
                    null
            ));
            log.info("检测结果不合格，发布不合格事件，检测结果ID：{}，样品ID：{}", result.getId(), result.getSampleId());
        }

        if (dto.getRawDataList() != null && !dto.getRawDataList().isEmpty()) {
            List<DetectRawData> rawDataList = new ArrayList<>();
            int sort = 1;
            for (DetectResultSubmitDTO.RawDataDTO rawDTO : dto.getRawDataList()) {
                DetectRawData rawData = new DetectRawData();
                rawData.setResultId(result.getId());
                rawData.setSampleId(dto.getSampleId());
                rawData.setDetectItemId(dto.getDetectItemId());
                rawData.setDataKey(rawDTO.getDataKey());
                rawData.setDataValue(rawDTO.getDataValue());
                rawData.setDataType(rawDTO.getDataType() != null ? rawDTO.getDataType() : "string");
                rawData.setSort(rawDTO.getSort() != null ? rawDTO.getSort() : sort++);
                rawData.setRemark(rawDTO.getRemark());
                rawData.setCreateTime(LocalDateTime.now());
                rawDataList.add(rawData);
            }
            detectRawDataMapper.insertBatch(rawDataList);
        }

        updateTaskCompletedCount(dto.getTaskId());

        return result;
    }

    @Override
    public DetectResultDetailVO getResultDetail(Long resultId) {
        DetectResult result = getById(resultId);
        if (result == null) {
            throw new BusinessException(ResultCode.RESULT_NOT_FOUND);
        }
        DetectResultDetailVO vo = BeanUtil.copyProperties(result, DetectResultDetailVO.class);
        List<DetectRawData> rawDataList = detectRawDataMapper.selectByResultId(resultId);
        List<DetectResultDetailVO.RawDataVO> rawDataVOS = rawDataList.stream()
                .map(rd -> BeanUtil.copyProperties(rd, DetectResultDetailVO.RawDataVO.class))
                .collect(Collectors.toList());
        vo.setRawDataList(rawDataVOS);
        return vo;
    }

    @Override
    public List<DetectResultDetailVO> getResultsByTaskId(Long taskId) {
        List<DetectResult> results = detectResultMapper.selectByTaskId(taskId);
        return results.stream()
                .map(result -> {
                    DetectResultDetailVO vo = BeanUtil.copyProperties(result, DetectResultDetailVO.class);
                    List<DetectRawData> rawDataList = detectRawDataMapper.selectByResultId(result.getId());
                    List<DetectResultDetailVO.RawDataVO> rawDataVOS = rawDataList.stream()
                            .map(rd -> BeanUtil.copyProperties(rd, DetectResultDetailVO.RawDataVO.class))
                            .collect(Collectors.toList());
                    vo.setRawDataList(rawDataVOS);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DetectResultDetailVO> getResultsBySampleId(Long sampleId) {
        List<DetectResult> results = detectResultMapper.selectBySampleId(sampleId);
        return results.stream()
                .map(result -> BeanUtil.copyProperties(result, DetectResultDetailVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public IPage<DetectResult> getResultPage(int pageNum, int pageSize, String sampleCode,
                                             String detectItemName, String finalJudge,
                                             String startDate, String endDate) {
        Page<DetectResult> page = new Page<>(pageNum, pageSize);
        return detectResultMapper.selectResultPage(page, sampleCode, detectItemName, finalJudge, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OfflineSyncResult syncOfflineData(DetectResultSyncDTO syncDTO) {
        OfflineSyncResult result = new OfflineSyncResult();
        List<Long> successIds = new ArrayList<>();
        List<OfflineSyncResult.SyncFailItem> failItems = new ArrayList<>();

        if (syncDTO.getDataList() == null || syncDTO.getDataList().isEmpty()) {
            result.setSuccess(true);
            result.setSuccessCount(0);
            result.setFailCount(0);
            result.setMessage("没有需要同步的数据");
            return result;
        }

        for (DetectResultSubmitDTO dto : syncDTO.getDataList()) {
            try {
                DetectResult detectResult = submitResult(dto, syncDTO.getUserId());
                successIds.add(detectResult.getId());
            } catch (Exception e) {
                log.error("检测结果离线同步失败: {}", e.getMessage(), e);
                OfflineSyncResult.SyncFailItem failItem = new OfflineSyncResult.SyncFailItem();
                failItem.setId(System.currentTimeMillis());
                failItem.setReason(e.getMessage());
                failItems.add(failItem);
            }
        }

        result.setSuccess(failItems.isEmpty());
        result.setSuccessCount(successIds.size());
        result.setFailCount(failItems.size());
        result.setSuccessIds(successIds);
        result.setFailItems(failItems);
        result.setMessage(failItems.isEmpty() ? "同步成功" : "部分数据同步失败");

        return result;
    }

    @Override
    public String autoJudge(Long detectItemId, DetectResultSubmitDTO dto) {
        List<LimitStandard> standards = limitStandardMapper.selectByDetectItemId(detectItemId);
        if (standards.isEmpty()) {
            return DetectConstants.JUDGE_PENDING;
        }

        LimitStandard standard = dto.getLimitStandardId() != null
                ? standards.stream().filter(s -> s.getId().equals(dto.getLimitStandardId())).findFirst().orElse(standards.get(0))
                : standards.get(0);

        if (DetectConstants.RESULT_TYPE_QUALITATIVE.equals(dto.getResultType())) {
            if (StrUtil.isNotBlank(standard.getQualitativeResult())) {
                return standard.getQualitativeResult().equals(dto.getQualitativeResult())
                        ? DetectConstants.JUDGE_QUALIFIED
                        : DetectConstants.JUDGE_UNQUALIFIED;
            }
            return DetectConstants.JUDGE_PENDING;
        }

        BigDecimal value = dto.getResultValue();
        if (value == null) {
            return DetectConstants.JUDGE_PENDING;
        }

        String limitType = standard.getLimitType();
        BigDecimal min = standard.getLimitValueMin();
        BigDecimal max = standard.getLimitValueMax();

        switch (limitType) {
            case "max":
                if (max != null && value.compareTo(max) > 0) {
                    return DetectConstants.JUDGE_UNQUALIFIED;
                }
                break;
            case "min":
                if (min != null && value.compareTo(min) < 0) {
                    return DetectConstants.JUDGE_UNQUALIFIED;
                }
                break;
            case "range":
                if (min != null && value.compareTo(min) < 0) {
                    return DetectConstants.JUDGE_UNQUALIFIED;
                }
                if (max != null && value.compareTo(max) > 0) {
                    return DetectConstants.JUDGE_UNQUALIFIED;
                }
                break;
            default:
                return DetectConstants.JUDGE_PENDING;
        }

        return DetectConstants.JUDGE_QUALIFIED;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSubmit(List<DetectResultSubmitDTO> dtoList, Long userId) {
        for (DetectResultSubmitDTO dto : dtoList) {
            submitResult(dto, userId);
        }
        return true;
    }

    private void updateTaskCompletedCount(Long taskId) {
        LambdaQueryWrapper<DetectResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DetectResult::getTaskId, taskId);
        Long completedCount = detectResultMapper.selectCount(wrapper);

        DetectTask task = detectTaskMapper.selectById(taskId);
        if (task != null) {
            task.setCompletedItemCount(completedCount.intValue());
            if (completedCount.intValue() >= task.getDetectItemCount() && task.getDetectItemCount() > 0) {
                detectTaskService.completeTask(taskId, task.getDetectPersonId());
                eventPublisher.publishEvent(new TaskCompletedEvent(
                        this,
                        task.getId(),
                        task.getTaskCode(),
                        task.getSampleId(),
                        task.getSampleCode(),
                        task.getDetectPersonId(),
                        task.getDetectPersonName()
                ));
                log.info("检测任务完成，发布任务完成事件，任务ID：{}", taskId);
            } else {
                detectTaskMapper.updateById(task);
            }
        }
    }
}
