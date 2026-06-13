package com.foodlab.schedule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ScheduleGenerateDTO {

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    private String algorithmType = "GREEDY";

    private List<Long> taskIds;

    private List<Long> instrumentIds;

    private List<Long> detectPersonIds;

    private boolean considerStaffLeave = true;

    private boolean considerInstrumentCalendar = true;

    private boolean autoPublish = false;
}
