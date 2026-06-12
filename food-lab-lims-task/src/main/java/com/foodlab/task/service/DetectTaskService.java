package com.foodlab.task.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodlab.task.dto.TaskAssignDTO;
import com.foodlab.task.dto.TaskQueryDTO;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.vo.TaskDetailVO;

import java.util.List;

public interface DetectTaskService extends IService<DetectTask> {

    DetectTask assignTask(TaskAssignDTO dto, Long assignUserId, String assignUserName);

    TaskDetailVO getTaskDetail(Long taskId);

    IPage<DetectTask> getTaskPage(int pageNum, int pageSize, TaskQueryDTO queryDTO);

    boolean startTask(Long taskId, Long userId);

    boolean submitTask(Long taskId, Long userId);

    boolean updateTaskStatus(Long taskId, String status, Long operatorId, String operatorName, String remark);

    boolean cancelTask(Long taskId, Long userId);

    List<DetectTask> getMyTasks(Long userId, String status);

    boolean autoAssign(Long sampleId, Long detectItemId);

    boolean completeTask(Long taskId, Long userId);
}
