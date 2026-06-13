package com.foodlab.ncr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.ncr.entity.NcrRecheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NcrRecheckMapper extends BaseMapper<NcrRecheck> {

    @Select("SELECT * FROM ncr_recheck WHERE ncr_id = #{ncrId} AND deleted = 0 ORDER BY recheck_count DESC")
    List<NcrRecheck> selectByNcrId(@Param("ncrId") Long ncrId);

    @Select("SELECT MAX(recheck_count) FROM ncr_recheck WHERE ncr_id = #{ncrId} AND deleted = 0")
    Integer getMaxRecheckCount(@Param("ncrId") Long ncrId);
}
