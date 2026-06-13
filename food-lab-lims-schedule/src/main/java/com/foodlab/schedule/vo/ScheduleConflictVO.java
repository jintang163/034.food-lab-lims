package com.foodlab.schedule.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleConflictVO {

    private Long id;
    private String conflictType;
    private String conflictDescription;
    private Long scheduleId1;
    private Long scheduleId2;
    private String taskName1;
    private String taskName2;
    private Long resourceId;
    private String resourceName;
    private String resourceType;
    private LocalDateTime conflictStart;
    private LocalDateTime conflictEnd;
    private String severity;
}
