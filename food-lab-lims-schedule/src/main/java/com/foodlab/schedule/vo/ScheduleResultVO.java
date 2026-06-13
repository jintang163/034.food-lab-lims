package com.foodlab.schedule.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ScheduleResultVO {

    private Long id;
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
    private LocalDateTime createTime;
}
