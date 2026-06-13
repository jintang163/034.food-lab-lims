package com.foodlab.ncr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.ncr.entity.NcrCauseAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NcrCauseAnalysisMapper extends BaseMapper<NcrCauseAnalysis> {

    @Select("SELECT * FROM ncr_cause_analysis WHERE ncr_id = #{ncrId} AND deleted = 0 ORDER BY id DESC LIMIT 1")
    NcrCauseAnalysis selectLatestByNcrId(@Param("ncrId") Long ncrId);
}
