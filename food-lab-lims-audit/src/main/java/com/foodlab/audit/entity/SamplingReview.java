package com.foodlab.audit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sampling_review")
public class SamplingReview extends BaseEntity {

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

    private String remark;
}
