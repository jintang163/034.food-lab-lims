package com.foodlab.sample.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sample_info")
public class Sample extends BaseEntity {

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

    private String syncStatus;

    private String offlineId;

    private String deviceId;

    private Integer detectItemCount;

    private String remark;

    private String attachFile;

    private String barCode;

    private String qrCode;
}
