package com.foodlab.ncr.dto;

import lombok.Data;

@Data
public class NcrQueryDTO {

    private String sampleCode;

    private String detectItemName;

    private String ncrStatus;

    private String ncrSource;

    private String startDate;

    private String endDate;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
