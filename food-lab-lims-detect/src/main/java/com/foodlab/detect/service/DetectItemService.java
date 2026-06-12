package com.foodlab.detect.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodlab.detect.dto.DetectItemQueryDTO;
import com.foodlab.detect.entity.DetectItem;
import com.foodlab.detect.vo.DetectItemVO;

import java.util.List;

public interface DetectItemService extends IService<DetectItem> {

    IPage<DetectItem> getDetectItemPage(int pageNum, int pageSize, DetectItemQueryDTO queryDTO);

    DetectItemVO getDetectItemDetail(Long id);

    List<DetectItemVO> getDetectItemList(Long categoryId);

    List<DetectItemVO> getAllDetectItems();

    String getFormSchema(Long detectItemId);

    String getItemNameById(Long id);
}
