package com.foodlab.sample.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.sample.entity.SampleDetectItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SampleDetectItemMapper extends BaseMapper<SampleDetectItem> {

    List<SampleDetectItem> selectBySampleId(@Param("sampleId") Long sampleId);

    int deleteBySampleId(@Param("sampleId") Long sampleId);

    int insertBatch(@Param("list") List<SampleDetectItem> list);
}
