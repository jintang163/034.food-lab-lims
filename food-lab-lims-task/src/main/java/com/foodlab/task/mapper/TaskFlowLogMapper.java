package com.foodlab.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.task.entity.TaskFlowLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskFlowLogMapper extends BaseMapper<TaskFlowLog> {

    List<TaskFlowLog> selectByTaskId(@Param("taskId") Long taskId);
}
