package com.foodlab.detect.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.common.result.ResultCode;
import com.foodlab.detect.dto.DetectItemQueryDTO;
import com.foodlab.detect.entity.DetectItem;
import com.foodlab.detect.entity.LimitStandard;
import com.foodlab.detect.mapper.DetectItemMapper;
import com.foodlab.detect.mapper.LimitStandardMapper;
import com.foodlab.detect.service.DetectItemService;
import com.foodlab.detect.vo.DetectItemVO;
import com.foodlab.detect.vo.LimitStandardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetectItemServiceImpl extends ServiceImpl<DetectItemMapper, DetectItem> implements DetectItemService {

    private final DetectItemMapper detectItemMapper;
    private final LimitStandardMapper limitStandardMapper;

    @Override
    public IPage<DetectItem> getDetectItemPage(int pageNum, int pageSize, DetectItemQueryDTO queryDTO) {
        Page<DetectItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DetectItem> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(queryDTO.getItemName())) {
            wrapper.like(DetectItem::getItemName, queryDTO.getItemName());
        }
        if (StrUtil.isNotBlank(queryDTO.getItemCode())) {
            wrapper.like(DetectItem::getItemCode, queryDTO.getItemCode());
        }
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(DetectItem::getCategoryId, queryDTO.getCategoryId());
        }
        if (StrUtil.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq(DetectItem::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(DetectItem::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public DetectItemVO getDetectItemDetail(Long id) {
        DetectItem item = detectItemMapper.selectDetailById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.DETECT_ITEM_NOT_FOUND);
        }
        DetectItemVO vo = BeanUtil.copyProperties(item, DetectItemVO.class);
        List<LimitStandard> standards = limitStandardMapper.selectByDetectItemId(id);
        List<LimitStandardVO> standardVOS = standards.stream()
                .map(s -> BeanUtil.copyProperties(s, LimitStandardVO.class))
                .collect(Collectors.toList());
        vo.setLimitStandards(standardVOS);
        return vo;
    }

    @Override
    public List<DetectItemVO> getDetectItemList(Long categoryId) {
        List<DetectItem> items;
        if (categoryId != null) {
            items = detectItemMapper.selectByCategoryId(categoryId);
        } else {
            items = detectItemMapper.selectAllWithCategory();
        }
        return items.stream()
                .map(item -> BeanUtil.copyProperties(item, DetectItemVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<DetectItemVO> getAllDetectItems() {
        List<DetectItem> items = detectItemMapper.selectAllWithCategory();
        return items.stream()
                .map(item -> {
                    DetectItemVO vo = BeanUtil.copyProperties(item, DetectItemVO.class);
                    List<LimitStandard> standards = limitStandardMapper.selectByDetectItemId(item.getId());
                    List<LimitStandardVO> standardVOS = standards.stream()
                            .map(s -> BeanUtil.copyProperties(s, LimitStandardVO.class))
                            .collect(Collectors.toList());
                    vo.setLimitStandards(standardVOS);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public String getFormSchema(Long detectItemId) {
        DetectItem item = getById(detectItemId);
        if (item == null) {
            throw new BusinessException(ResultCode.DETECT_ITEM_NOT_FOUND);
        }
        return item.getFormSchema();
    }

    @Override
    public String getItemNameById(Long id) {
        return detectItemMapper.selectItemNameById(id);
    }
}
