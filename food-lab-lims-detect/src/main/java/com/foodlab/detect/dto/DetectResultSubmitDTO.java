package com.foodlab.detect.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DetectResultSubmitDTO {

    private Long taskId;

    private Long sampleId;

    private String sampleCode;

    private Long detectItemId;

    private String instrument;

    private LocalDateTime detectTime;

    private String resultType;

    private BigDecimal resultValue;

    private String resultUnit;

    private String qualitativeResult;

    private Long limitStandardId;

    private String calculateFormula;

    private String remark;

    private String attachFiles;

    private List<RawDataDTO> rawDataList;

    @Data
    public static class RawDataDTO {
        private String dataKey;
        private String dataValue;
        private String dataType;
        private Integer sort;
        private String remark;
    }
}
