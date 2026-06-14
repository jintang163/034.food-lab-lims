package com.foodlab.audit.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SamplingReviewVO {

    private Long id;

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

    private LocalDateTime createTime;
}
