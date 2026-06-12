package com.foodlab.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_flow_log")
public class TaskFlowLog extends BaseEntity {

    private Long taskId;

    private String taskCode;

    private String nodeName;

    private String nodeCode;

    private String operationType;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime operateTime;

    private String remark;

    private String fromStatus;

    private String toStatus;
}
