package com.foodlab.sample.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodlab.common.constant.RedisConstants;
import com.foodlab.common.constant.SampleConstants;
import com.foodlab.common.domain.OfflineSyncResult;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.common.result.ResultCode;
import com.foodlab.common.utils.CodeGenerator;
import com.foodlab.common.utils.JsonUtils;
import com.foodlab.detect.service.DetectItemService;
import com.foodlab.sample.dto.SampleRegisterDTO;
import com.foodlab.sample.dto.SampleSyncDTO;
import com.foodlab.sample.entity.Sample;
import com.foodlab.sample.entity.SampleDetectItem;
import com.foodlab.sample.excel.SampleExcelVO;
import com.foodlab.sample.mapper.SampleDetectItemMapper;
import com.foodlab.sample.mapper.SampleMapper;
import com.foodlab.sample.service.SampleService;
import com.foodlab.sample.vo.SampleDetailVO;
import com.foodlab.sample.vo.SampleDetectItemVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleServiceImpl extends ServiceImpl<SampleMapper, Sample> implements SampleService {

    private final SampleDetectItemMapper sampleDetectItemMapper;
    private final StringRedisTemplate redisTemplate;
    private final DetectItemService detectItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Sample registerSample(SampleRegisterDTO dto, Long userId) {
        String sampleCode = CodeGenerator.generateSampleCode();
        String barCode = CodeGenerator.generateBarcode(sampleCode);
        String qrCode = CodeGenerator.generateQRCodeContent(sampleCode, "SAMPLE");

        Sample sample = BeanUtil.copyProperties(dto, Sample.class);
        sample.setSampleCode(sampleCode);
        sample.setBarCode(barCode);
        sample.setQrCode(qrCode);
        sample.setSampleStatus(SampleConstants.SAMPLE_STATUS_PENDING);
        sample.setSyncStatus(SampleConstants.SYNC_STATUS_SUCCESS);
        sample.setDetectItemCount(dto.getDetectItemIds() != null ? dto.getDetectItemIds().size() : 0);
        sample.setCreateBy(userId);
        sample.setUpdateBy(userId);

        save(sample);

        if (dto.getDetectItemIds() != null && !dto.getDetectItemIds().isEmpty()) {
            List<SampleDetectItem> detectItems = new ArrayList<>();
            int sort = 1;
            for (Long itemId : dto.getDetectItemIds()) {
                SampleDetectItem item = new SampleDetectItem();
                item.setSampleId(sample.getId());
                item.setSampleCode(sampleCode);
                item.setDetectItemId(itemId);
                String itemName = detectItemService.getItemNameById(itemId);
                item.setDetectItemName(itemName);
                item.setSort(sort++);
                detectItems.add(item);
            }
            sampleDetectItemMapper.insertBatch(detectItems);
        }

        cacheSample(sample);

        return sample;
    }

    @Override
    public SampleDetailVO getSampleDetail(Long sampleId) {
        String cacheKey = RedisConstants.SAMPLE_CACHE_KEY + sampleId;
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(cacheValue)) {
            return JsonUtils.parse(cacheValue, SampleDetailVO.class);
        }

        Sample sample = getById(sampleId);
        if (sample == null) {
            throw new BusinessException(ResultCode.SAMPLE_NOT_FOUND);
        }

        SampleDetailVO vo = BeanUtil.copyProperties(sample, SampleDetailVO.class);

        List<SampleDetectItem> detectItems = sampleDetectItemMapper.selectBySampleId(sampleId);
        List<SampleDetectItemVO> itemVOS = detectItems.stream()
                .map(item -> BeanUtil.copyProperties(item, SampleDetectItemVO.class))
                .collect(Collectors.toList());
        vo.setDetectItems(itemVOS);

        redisTemplate.opsForValue().set(cacheKey, JsonUtils.toJson(vo),
                RedisConstants.SAMPLE_EXPIRE_TIME, TimeUnit.SECONDS);

        return vo;
    }

    @Override
    public IPage<Sample> getSamplePage(int pageNum, int pageSize, String sampleName,
                                       String sampleCode, String sampleStatus,
                                       String startDate, String endDate) {
        Page<Sample> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectSamplePage(page, sampleName, sampleCode, sampleStatus, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSample(Long sampleId, SampleRegisterDTO dto) {
        Sample sample = getById(sampleId);
        if (sample == null) {
            throw new BusinessException(ResultCode.SAMPLE_NOT_FOUND);
        }

        BeanUtil.copyProperties(dto, sample, "id", "sampleCode", "createTime", "createBy");
        if (dto.getDetectItemIds() != null) {
            sample.setDetectItemCount(dto.getDetectItemIds().size());
        }

        boolean result = updateById(sample);

        if (dto.getDetectItemIds() != null && !dto.getDetectItemIds().isEmpty()) {
            sampleDetectItemMapper.deleteBySampleId(sampleId);
            List<SampleDetectItem> detectItems = new ArrayList<>();
            int sort = 1;
            for (Long itemId : dto.getDetectItemIds()) {
                SampleDetectItem item = new SampleDetectItem();
                item.setSampleId(sampleId);
                item.setSampleCode(sample.getSampleCode());
                item.setDetectItemId(itemId);
                String itemName = detectItemService.getItemNameById(itemId);
                item.setDetectItemName(itemName);
                item.setSort(sort++);
                detectItems.add(item);
            }
            sampleDetectItemMapper.insertBatch(detectItems);
        }

        evictSampleCache(sampleId);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSample(Long sampleId) {
        boolean result = removeById(sampleId);
        if (result) {
            sampleDetectItemMapper.deleteBySampleId(sampleId);
            evictSampleCache(sampleId);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(List<Long> ids) {
        for (Long id : ids) {
            deleteSample(id);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OfflineSyncResult syncOfflineData(SampleSyncDTO syncDTO) {
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

        for (SampleRegisterDTO dto : syncDTO.getDataList()) {
            try {
                Sample sample = registerSample(dto, syncDTO.getUserId());
                successIds.add(sample.getId());
            } catch (Exception e) {
                log.error("离线数据同步失败: {}", e.getMessage(), e);
                OfflineSyncResult.SyncFailItem failItem = new OfflineSyncResult.SyncFailItem();
                failItem.setId(Long.valueOf(dto.getOfflineId() != null ? dto.getOfflineId() : "0"));
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
    public String generateBarcode(Long sampleId) {
        Sample sample = getById(sampleId);
        if (sample == null) {
            throw new BusinessException(ResultCode.SAMPLE_NOT_FOUND);
        }
        return sample.getBarCode();
    }

    @Override
    public String generateQRCode(Long sampleId) {
        Sample sample = getById(sampleId);
        if (sample == null) {
            throw new BusinessException(ResultCode.SAMPLE_NOT_FOUND);
        }
        return sample.getQrCode();
    }

    @Override
    public void exportSample(List<Long> ids, HttpServletResponse response) {
        List<Sample> samples;
        if (ids != null && !ids.isEmpty()) {
            samples = listByIds(ids);
        } else {
            samples = list();
        }

        List<SampleExcelVO> excelVOS = samples.stream()
                .map(sample -> {
                    SampleExcelVO vo = BeanUtil.copyProperties(sample, SampleExcelVO.class);
                    List<SampleDetectItem> items = sampleDetectItemMapper.selectBySampleId(sample.getId());
                    String detectItemNames = items.stream()
                            .map(SampleDetectItem::getDetectItemName)
                            .collect(Collectors.joining("、"));
                    vo.setDetectItems(detectItemNames);
                    return vo;
                })
                .collect(Collectors.toList());

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("样品数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SampleExcelVO.class)
                    .sheet("样品列表")
                    .doWrite(excelVOS);
        } catch (IOException e) {
            log.error("导出样品数据失败", e);
            throw new BusinessException(ResultCode.FAIL, "导出失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importSample(MultipartFile file, Long userId) {
        try {
            List<SampleExcelVO> excelVOS = EasyExcel.read(file.getInputStream())
                    .head(SampleExcelVO.class)
                    .sheet()
                    .doReadSync();

            for (SampleExcelVO vo : excelVOS) {
                SampleRegisterDTO dto = new SampleRegisterDTO();
                dto.setSampleName(vo.getSampleName());
                dto.setBatchNo(vo.getBatchNo());
                dto.setManufacturer(vo.getManufacturer());
                dto.setProductionDate(vo.getProductionDate());
                dto.setShelfLife(vo.getShelfLife());
                dto.setSampleLocation(vo.getSampleLocation());
                dto.setSampleMethod(vo.getSampleMethod());
                dto.setSamplePerson(vo.getSamplePerson());
                dto.setSampleAmount(vo.getSampleAmount());
                dto.setSampleUnit(vo.getSampleUnit());
                dto.setRemark(vo.getRemark());
                registerSample(dto, userId);
            }
        } catch (IOException e) {
            log.error("导入样品数据失败", e);
            throw new BusinessException(ResultCode.FAIL, "导入失败");
        }
    }

    @Override
    public boolean updateSampleStatus(Long sampleId, String status) {
        Sample sample = getById(sampleId);
        if (sample == null) {
            throw new BusinessException(ResultCode.SAMPLE_NOT_FOUND);
        }
        sample.setSampleStatus(status);
        boolean result = updateById(sample);
        if (result) {
            evictSampleCache(sampleId);
        }
        return result;
    }

    private void cacheSample(Sample sample) {
        String cacheKey = RedisConstants.SAMPLE_CACHE_KEY + sample.getId();
        SampleDetailVO vo = BeanUtil.copyProperties(sample, SampleDetailVO.class);
        redisTemplate.opsForValue().set(cacheKey, JsonUtils.toJson(vo),
                RedisConstants.SAMPLE_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    private void evictSampleCache(Long sampleId) {
        String cacheKey = RedisConstants.SAMPLE_CACHE_KEY + sampleId;
        redisTemplate.delete(cacheKey);
    }
}
