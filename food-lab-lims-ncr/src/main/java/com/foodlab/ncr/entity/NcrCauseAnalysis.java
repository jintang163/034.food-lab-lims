package com.foodlab.ncr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ncr_cause_analysis")
public class NcrCauseAnalysis extends BaseEntity {

    private Long ncrId;

    private String ncrCode;

    private String causeType;

    private String causeDescription;

    private String rootCause;

    private String impactAnalysis;

    private Long analysisPersonId;

    private String analysisPersonName;

    private LocalDateTime analysisTime;

    private String reviewPersonId;

    private String reviewPersonName;

    private LocalDateTime reviewTime;

    private String reviewOpinion;

    private String remark;

    private String attachFiles;
}
