package com.foodlab.ncr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.ncr.entity.NcrCorrectiveAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NcrCorrectiveActionMapper extends BaseMapper<NcrCorrectiveAction> {

    @Select("SELECT * FROM ncr_corrective_action WHERE ncr_id = #{ncrId} AND deleted = 0 ORDER BY create_time DESC")
    List<NcrCorrectiveAction> selectByNcrId(@Param("ncrId") Long ncrId);
}
