package com.foodlab.detect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.detect.entity.LimitStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LimitStandardMapper extends BaseMapper<LimitStandard> {

    @Select("SELECT * FROM limit_standard WHERE detect_item_id = #{detectItemId} AND status = '0' AND deleted = 0")
    List<LimitStandard> selectByDetectItemId(@Param("detectItemId") Long detectItemId);
}
