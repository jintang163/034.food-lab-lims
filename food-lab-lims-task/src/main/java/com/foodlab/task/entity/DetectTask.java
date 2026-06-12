package com.foodlab.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("detect_task")
public class DetectTask extends BaseEntity {

    private String taskCode;

    private Long sampleId;

    private String sampleCode;

    private String taskName;

    private String taskType;

    private String priority;

    private String taskStatus;

    private Long assignBy;

    private String assignByName;

    private LocalDateTime assignTime;

    private Long detectPersonId;

    private String detectPersonName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String remark;

    private String processInstanceId;

    private Integer detectItemCount;

    private Integer completedItemCount;
}
