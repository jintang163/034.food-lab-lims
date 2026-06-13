package com.foodlab.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("instrument_calendar")
public class InstrumentCalendar extends BaseEntity {

    private Long instrumentId;
    private LocalDate calendarDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String eventType;
    private Long taskId;
    private String title;
    private String remark;
}
