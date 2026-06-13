package com.foodlab.ncr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.ncr.entity.NcrEventCompensation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NcrEventCompensationMapper extends BaseMapper<NcrEventCompensation> {

    @Select("SELECT * FROM ncr_event_compensation WHERE compensation_status IN ('pending', 'failed') " +
            "AND next_retry_time <= #{now} AND retry_count < max_retry AND deleted = 0 " +
            "ORDER BY next_retry_time ASC LIMIT #{limit}")
    List<NcrEventCompensation> selectPendingRetry(@Param("now") LocalDateTime now, @Param("limit") int limit);

    @Update("UPDATE ncr_event_compensation SET compensation_status = 'processing', update_time = NOW() " +
            "WHERE id = #{id} AND compensation_status IN ('pending', 'failed') AND deleted = 0")
    int lockForProcessing(@Param("id") Long id);

    @Select("SELECT * FROM ncr_event_compensation WHERE event_type = #{eventType} AND biz_key = #{bizKey} AND deleted = 0 LIMIT 1")
    NcrEventCompensation selectByEventAndBizKey(@Param("eventType") String eventType, @Param("bizKey") String bizKey);
}
