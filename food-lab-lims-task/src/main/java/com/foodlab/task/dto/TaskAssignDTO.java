package com.foodlab.task.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class TaskAssignDTO implements Serializable {

    @NotNull(message = "样品ID不能为空")
    private Long sampleId;

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    private String taskType;

    private String priority;

    @NotNull(message = "检测人员不能为空")
    private Long detectPersonId;

    private String remark;

    private List<Long> detectItemIds;
}
