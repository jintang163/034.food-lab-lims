package com.foodlab.ncr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.ncr.entity.NcrPreventiveAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NcrPreventiveActionMapper extends BaseMapper<NcrPreventiveAction> {

    @Select("SELECT * FROM ncr_preventive_action WHERE ncr_id = #{ncrId} AND deleted = 0 ORDER BY create_time DESC")
    List<NcrPreventiveAction> selectByNcrId(@Param("ncrId") Long ncrId);
}
