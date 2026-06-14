package com.foodlab.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.audit.entity.RetestRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RetestRecordMapper extends BaseMapper<RetestRecord> {

    @Select("SELECT * FROM retest_record WHERE task_id = #{taskId} AND deleted = 0 ORDER BY create_time DESC")
    List<RetestRecord> selectByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT * FROM retest_record WHERE trigger_audit_id = #{auditId} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    RetestRecord selectLatestByAuditId(@Param("auditId") Long auditId);

    @Select("SELECT * FROM retest_record WHERE original_result_id = #{resultId} AND deleted = 0 ORDER BY create_time DESC")
    List<RetestRecord> selectByOriginalResultId(@Param("resultId") Long resultId);
}
