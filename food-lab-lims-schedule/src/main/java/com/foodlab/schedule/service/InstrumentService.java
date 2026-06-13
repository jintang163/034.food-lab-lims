package com.foodlab.schedule.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.schedule.entity.Instrument;
import com.foodlab.schedule.entity.InstrumentCalendar;

import java.time.LocalDate;
import java.util.List;

public interface InstrumentService {

    IPage<Instrument> getInstrumentPage(int pageNum, int pageSize, String keyword, String status);

    Instrument getInstrumentById(Long id);

    boolean saveInstrument(Instrument instrument);

    boolean updateInstrument(Instrument instrument);

    boolean deleteInstrument(Long id);

    List<InstrumentCalendar> getInstrumentCalendar(Long instrumentId, LocalDate startDate, LocalDate endDate);

    boolean addCalendarEvent(InstrumentCalendar calendar);

    boolean updateCalendarEvent(InstrumentCalendar calendar);

    boolean deleteCalendarEvent(Long id);

    List<Instrument> getAvailableInstrumentsByDetectItem(Long detectItemId);
}
