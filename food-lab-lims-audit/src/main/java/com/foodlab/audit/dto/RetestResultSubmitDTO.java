package com.foodlab.audit.dto;

import lombok.Data;

@Data
public class RetestResultSubmitDTO {

    private Long retestId;

    private String retestValue;

    private String retestJudge;
}
