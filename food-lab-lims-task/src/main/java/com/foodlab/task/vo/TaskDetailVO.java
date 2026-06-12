package com.foodlab.task.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDetailVO implements Serializable {

    private Long id;

    private String taskCode;

    private Long sampleId;

    private String sampleCode;

    private String taskName;

    private String taskType;

    private String priority;

    private String taskStatus;

    private String assignByName;

    private LocalDateTime assignTime;

    private String detectPersonName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String remark;

    private Integer detectItemCount;

    private Integer completedItemCount;

    private List<TaskFlowLogVO> flowLogs;
}
