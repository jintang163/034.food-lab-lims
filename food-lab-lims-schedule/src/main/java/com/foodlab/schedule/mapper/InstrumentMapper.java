package com.foodlab.schedule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.schedule.entity.Instrument;
import com.foodlab.schedule.entity.InstrumentCalendar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface InstrumentMapper extends BaseMapper<Instrument> {

    List<InstrumentCalendar> selectOccupiedSlots(@Param("instrumentId") Long instrumentId,
                                                 @Param("calendarDate") LocalDate calendarDate);

    List<InstrumentCalendar> selectOccupiedSlotsByRange(@Param("instrumentId") Long instrumentId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    List<Instrument> selectAvailableInstrumentsByDetectItem(@Param("detectItemId") Long detectItemId);
}
