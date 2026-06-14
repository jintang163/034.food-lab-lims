package com.foodlab.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodlab.task.dto.TaskQueryDTO;
import com.foodlab.task.entity.DetectTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DetectTaskMapper extends BaseMapper<DetectTask> {

    IPage<DetectTask> selectTaskPage(Page<DetectTask> page, @Param("query") TaskQueryDTO queryDTO);

    @Select("SELECT * FROM detect_task WHERE task_status = 'APPROVED' AND deleted = 0 ORDER BY create_time DESC")
    List<DetectTask> selectApprovedTasks();

    @Select("SELECT * FROM detect_task WHERE task_status = 'APPROVED' AND deleted = 0 AND id NOT IN "
            + "(SELECT task_id FROM sampling_review WHERE deleted = 0 AND task_id IS NOT NULL) "
            + "ORDER BY create_time DESC")
    List<DetectTask> selectApprovedTasksNotReviewed();
}
