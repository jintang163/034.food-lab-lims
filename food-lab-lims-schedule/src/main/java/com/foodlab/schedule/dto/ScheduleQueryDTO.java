package com.foodlab.schedule.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ScheduleQueryDTO {

    private String scheduleBatchNo;
    private Long taskId;
    private Long instrumentId;
    private Long detectPersonId;
    private LocalDate scheduleDateStart;
    private LocalDate scheduleDateEnd;
    private String priority;
    private List<String> statusList;
}
