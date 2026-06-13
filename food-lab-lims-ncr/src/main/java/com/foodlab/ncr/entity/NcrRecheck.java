package com.foodlab.ncr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ncr_recheck")
public class NcrRecheck extends BaseEntity {

    private Long ncrId;

    private String ncrCode;

    private Integer recheckCount;

    private String recheckStatus;

    private Long taskId;

    private Long sampleId;

    private String sampleCode;

    private Long detectItemId;

    private String detectItemName;

    private String detectMethod;

    private String detectStandard;

    private String instrument;

    private LocalDateTime detectTime;

    private Long detectPersonId;

    private String detectPersonName;

    private String resultType;

    private BigDecimal resultValue;

    private String resultUnit;

    private String qualitativeResult;

    private String limitType;

    private BigDecimal limitValueMin;

    private BigDecimal limitValueMax;

    private String finalJudge;

    private String recheckRemark;

    private String attachFiles;
}
