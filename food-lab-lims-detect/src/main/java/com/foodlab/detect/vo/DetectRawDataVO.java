package com.foodlab.detect.vo;

import lombok.Data;

@Data
public class DetectRawDataVO {

    private Long id;

    private String dataKey;

    private String dataValue;

    private String dataType;

    private Integer sort;

    private String remark;
}
