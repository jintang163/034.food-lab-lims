package com.foodlab.schedule.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleGanttVO {

    private Long id;
    private String taskId;
    private String name;
    private Long instrumentId;
    private String instrumentName;
    private Long detectPersonId;
    private String detectPersonName;
    private LocalDateTime start;
    private LocalDateTime end;
    private Integer duration;
    private String priority;
    private String status;
    private String source;
    private String scheduleBatchNo;
    private String remark;
    private String color;
    private Boolean canDrag;
    private Boolean canResize;
}
