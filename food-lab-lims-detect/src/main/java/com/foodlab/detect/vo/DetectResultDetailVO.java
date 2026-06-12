package com.foodlab.detect.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DetectResultDetailVO {

    private Long id;

    private String resultCode;

    private Long taskId;

    private Long sampleId;

    private String sampleCode;

    private Long detectItemId;

    private String detectItemName;

    private String detectMethod;

    private String detectStandard;

    private String instrument;

    private LocalDateTime detectTime;

    private String detectPersonName;

    private String resultType;

    private BigDecimal resultValue;

    private String resultUnit;

    private String qualitativeResult;

    private String limitType;

    private BigDecimal limitValueMin;

    private BigDecimal limitValueMax;

    private String autoJudge;

    private String manualJudge;

    private String finalJudge;

    private String calculateFormula;

    private String remark;

    private String attachFiles;

    private String isAudit;

    private List<RawDataVO> rawDataList;

    @Data
    public static class RawDataVO {
        private Long id;
        private String dataKey;
        private String dataValue;
        private String dataType;
        private Integer sort;
        private String remark;
    }
}
