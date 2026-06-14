package com.foodlab.audit.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sampling_review")
public class SamplingReview extends BaseEntity {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    private String taskCode;

    private String sampleCode;

    private Double sampleRate;

    private String reviewType;

    private String reviewStatus;

    private Long reviewerId;

    private String reviewerName;

    private LocalDateTime reviewTime;

    private String reviewOpinion;

    @TableField(exist = false)
    private List<Long> taskIds;

    private String taskIdsStr;

    @TableField(exist = false)
    private List<com.foodlab.task.entity.DetectTask> sampledTasks;

    private String remark;
}
