package com.foodlab.audit.dto;

import lombok.Data;

@Data
public class SamplingReviewDTO {

    private Double sampleRate;

    private String reviewType;

    private String remark;
}
