package com.foodlab.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodlab.task.dto.TaskQueryDTO;
import com.foodlab.task.entity.DetectTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DetectTaskMapper extends BaseMapper<DetectTask> {

    IPage<DetectTask> selectTaskPage(Page<DetectTask> page, @Param("query") TaskQueryDTO queryDTO);
}
