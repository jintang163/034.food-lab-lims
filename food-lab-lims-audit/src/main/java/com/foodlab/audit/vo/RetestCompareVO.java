package com.foodlab.audit.vo;

import lombok.Data;

@Data
public class RetestCompareVO {

    private Long retestId;

    private String retestCode;

    private String originalValue;

    private String originalJudge;

    private String retestValue;

    private String retestJudge;

    private Boolean valueChanged;

    private Boolean judgeChanged;

    private String adoptedResult;

    private String adoptOpinion;
}
