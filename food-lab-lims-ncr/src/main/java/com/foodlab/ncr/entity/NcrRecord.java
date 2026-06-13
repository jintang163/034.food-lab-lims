package com.foodlab.ncr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ncr_record")
public class NcrRecord extends BaseEntity {

    private String ncrCode;

    private String ncrSource;

    private Long sampleId;

    private String sampleCode;

    private String sampleName;

    private Long detectResultId;

    private Long detectItemId;

    private String detectItemName;

    private String unqualifiedDescription;

    private String ncrStatus;

    private String currentStage;

    private LocalDateTime recheckTime;

    private LocalDateTime causeAnalysisTime;

    private LocalDateTime correctiveActionTime;

    private LocalDateTime preventiveActionTime;

    private LocalDateTime closeTime;

    private Long closePersonId;

    private String closePersonName;

    private String closeRemark;

    private String remark;

    private String attachFiles;
}
