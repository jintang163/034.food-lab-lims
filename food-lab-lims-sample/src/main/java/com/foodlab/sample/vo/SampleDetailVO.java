package com.foodlab.sample.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SampleDetailVO implements Serializable {

    private Long id;

    private String sampleCode;

    private String sampleName;

    private String batchNo;

    private String manufacturer;

    private String productionDate;

    private String shelfLife;

    private String sampleLocation;

    private String sampleMethod;

    private LocalDateTime sampleTime;

    private String samplePerson;

    private String sampleAmount;

    private String sampleUnit;

    private String sampleStatus;

    private String barCode;

    private String qrCode;

    private Integer detectItemCount;

    private String remark;

    private String attachFile;

    private LocalDateTime createTime;

    private List<SampleDetectItemVO> detectItems;
}
