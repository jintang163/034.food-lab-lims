package com.foodlab.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("schedule_log")
public class ScheduleLog extends BaseEntity {

    private String scheduleBatchNo;
    private String operationType;
    private Integer taskCount;
    private Integer conflictCount;
    private String algorithmType;
    private Long timeCostMs;
    private String remark;
}
