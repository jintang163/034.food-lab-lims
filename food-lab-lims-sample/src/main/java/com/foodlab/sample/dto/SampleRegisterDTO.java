package com.foodlab.sample.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SampleRegisterDTO implements Serializable {

    @NotBlank(message = "样品名称不能为空")
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

    private String remark;

    private String attachFile;

    private String offlineId;

    private String deviceId;

    private List<Long> detectItemIds;
}
