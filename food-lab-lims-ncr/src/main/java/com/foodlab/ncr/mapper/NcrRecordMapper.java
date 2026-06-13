package com.foodlab.ncr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodlab.ncr.entity.NcrRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NcrRecordMapper extends BaseMapper<NcrRecord> {

    @Select("SELECT * FROM ncr_record WHERE sample_id = #{sampleId} AND deleted = 0 ORDER BY create_time DESC")
    List<NcrRecord> selectBySampleId(@Param("sampleId") Long sampleId);

    @Select("SELECT * FROM ncr_record WHERE detect_result_id = #{detectResultId} AND deleted = 0 LIMIT 1")
    NcrRecord selectByDetectResultId(@Param("detectResultId") Long detectResultId);

    IPage<NcrRecord> selectNcrPage(Page<NcrRecord> page,
                                   @Param("sampleCode") String sampleCode,
                                   @Param("detectItemName") String detectItemName,
                                   @Param("ncrStatus") String ncrStatus,
                                   @Param("ncrSource") String ncrSource,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate);
}
