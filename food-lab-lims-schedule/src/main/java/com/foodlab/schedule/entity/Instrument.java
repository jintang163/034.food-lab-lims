package com.foodlab.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("instrument")
public class Instrument extends BaseEntity {

    private String instrumentCode;
    private String instrumentName;
    private String instrumentType;
    private String status;
    private String location;
    private LocalTime dailyStartTime;
    private LocalTime dailyEndTime;
    private Integer maintenanceCycleDays;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDate;
    private String remark;
}
