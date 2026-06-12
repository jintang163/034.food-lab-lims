package com.foodlab.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodlab.report.entity.DetectReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DetectReportMapper extends BaseMapper<DetectReport> {

    @Select("SELECT * FROM detect_report WHERE sample_id = #{sampleId} AND deleted = 0 ORDER BY create_time DESC")
    List<DetectReport> selectBySampleId(@Param("sampleId") Long sampleId);

    @Select("SELECT * FROM detect_report WHERE task_id = #{taskId} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    DetectReport selectByTaskId(@Param("taskId") Long taskId);

    IPage<DetectReport> selectReportPage(Page<DetectReport> page,
                                          @Param("reportCode") String reportCode,
                                          @Param("sampleCode") String sampleCode,
                                          @Param("reportStatus") String reportStatus,
                                          @Param("reportType") String reportType,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate);
}
