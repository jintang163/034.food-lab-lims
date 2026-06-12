package com.foodlab.detect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.detect.entity.DetectRawData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DetectRawDataMapper extends BaseMapper<DetectRawData> {

    @Select("SELECT * FROM detect_raw_data WHERE result_id = #{resultId} ORDER BY sort")
    List<DetectRawData> selectByResultId(@Param("resultId") Long resultId);

    @Select("SELECT * FROM detect_raw_data WHERE sample_id = #{sampleId} AND detect_item_id = #{detectItemId} ORDER BY sort")
    List<DetectRawData> selectBySampleAndItem(@Param("sampleId") Long sampleId, @Param("detectItemId") Long detectItemId);

    void insertBatch(@Param("list") List<DetectRawData> list);

    void deleteByResultId(@Param("resultId") Long resultId);
}
