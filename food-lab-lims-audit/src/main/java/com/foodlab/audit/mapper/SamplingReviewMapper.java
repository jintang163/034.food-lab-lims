package com.foodlab.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.audit.entity.SamplingReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SamplingReviewMapper extends BaseMapper<SamplingReview> {

    @Select("SELECT * FROM sampling_review WHERE task_id = #{taskId} AND deleted = 0 ORDER BY create_time DESC")
    List<SamplingReview> selectByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT * FROM sampling_review WHERE review_status = 'PENDING' AND deleted = 0 ORDER BY create_time DESC")
    List<SamplingReview> selectPendingReviews();

    @Select("SELECT task_id FROM sampling_review WHERE deleted = 0")
    List<Long> selectAllReviewedTaskIds();
}
