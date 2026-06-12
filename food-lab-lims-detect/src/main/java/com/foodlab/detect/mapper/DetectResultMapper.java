package com.foodlab.detect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodlab.detect.entity.DetectResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DetectResultMapper extends BaseMapper<DetectResult> {

    @Select("SELECT * FROM detect_result WHERE task_id = #{taskId} AND deleted = 0")
    List<DetectResult> selectByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT * FROM detect_result WHERE sample_id = #{sampleId} AND deleted = 0")
    List<DetectResult> selectBySampleId(@Param("sampleId") Long sampleId);

    @Select("SELECT * FROM detect_result WHERE task_id = #{taskId} AND detect_item_id = #{detectItemId} AND deleted = 0 LIMIT 1")
    DetectResult selectByTaskAndItem(@Param("taskId") Long taskId, @Param("detectItemId") Long detectItemId);

    IPage<DetectResult> selectResultPage(Page<DetectResult> page,
                                         @Param("sampleCode") String sampleCode,
                                         @Param("detectItemName") String detectItemName,
                                         @Param("finalJudge") String finalJudge,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate);
}
