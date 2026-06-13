package com.foodlab.ncr.dto;

import lombok.Data;

@Data
public class NcrCreateDTO {

    private Long sampleId;

    private String sampleCode;

    private String sampleName;

    private Long detectResultId;

    private Long detectItemId;

    private String detectItemName;

    private String unqualifiedDescription;

    private String ncrSource;

    private String remark;

    private String attachFiles;
}
