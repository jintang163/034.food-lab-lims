package com.foodlab.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("schedule_result")
public class ScheduleResult extends BaseEntity {

    private String scheduleBatchNo;
    private Long taskId;
    private String taskCode;
    private Long sampleId;
    private String sampleCode;
    private Long detectItemId;
    private String detectItemName;
    private Long instrumentId;
    private String instrumentName;
    private Long detectPersonId;
    private String detectPersonName;
    private LocalDate scheduleDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String priority;
    private String status;
    private String source;
    private Integer sortOrder;
    private String remark;
}
