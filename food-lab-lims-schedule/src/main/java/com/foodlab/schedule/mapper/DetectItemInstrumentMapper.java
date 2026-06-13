package com.foodlab.schedule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.schedule.entity.DetectItemInstrument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DetectItemInstrumentMapper extends BaseMapper<DetectItemInstrument> {

    List<DetectItemInstrument> selectByDetectItemId(@Param("detectItemId") Long detectItemId);
}
