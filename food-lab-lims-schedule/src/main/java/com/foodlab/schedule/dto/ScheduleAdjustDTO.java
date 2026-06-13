package com.foodlab.schedule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleAdjustDTO {

    @NotNull(message = "排程ID不能为空")
    private Long id;

    @NotNull(message = "新开始时间不能为空")
    private LocalDateTime newStartTime;

    @NotNull(message = "新结束时间不能为空")
    private LocalDateTime newEndTime;

    private Long newInstrumentId;

    private Long newDetectPersonId;

    private String reason;
}
