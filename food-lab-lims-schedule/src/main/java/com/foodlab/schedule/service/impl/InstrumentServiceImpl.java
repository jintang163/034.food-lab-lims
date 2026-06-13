package com.foodlab.schedule.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.schedule.constant.ScheduleConstants;
import com.foodlab.schedule.entity.Instrument;
import com.foodlab.schedule.entity.InstrumentCalendar;
import com.foodlab.schedule.mapper.InstrumentCalendarMapper;
import com.foodlab.schedule.mapper.InstrumentMapper;
import com.foodlab.schedule.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstrumentServiceImpl implements InstrumentService {

    private final InstrumentMapper instrumentMapper;
    private final InstrumentCalendarMapper instrumentCalendarMapper;

    @Override
    public IPage<Instrument> getInstrumentPage(int pageNum, int pageSize, String keyword, String status) {
        LambdaQueryWrapper<Instrument> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Instrument::getInstrumentName, keyword)
                    .or().like(Instrument::getInstrumentCode, keyword));
        }
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Instrument::getStatus, status);
        }
        wrapper.orderByAsc(Instrument::getInstrumentCode);
        return instrumentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Instrument getInstrumentById(Long id) {
        return instrumentMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveInstrument(Instrument instrument) {
        LambdaQueryWrapper<Instrument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Instrument::getInstrumentCode, instrument.getInstrumentCode());
        if (instrumentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("仪器编号已存在");
        }
        return instrumentMapper.insert(instrument) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateInstrument(Instrument instrument) {
        return instrumentMapper.updateById(instrument) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteInstrument(Long id) {
        return instrumentMapper.deleteById(id) > 0;
    }

    @Override
    public List<InstrumentCalendar> getInstrumentCalendar(Long instrumentId, LocalDate startDate, LocalDate endDate) {
        return instrumentMapper.selectOccupiedSlotsByRange(instrumentId, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCalendarEvent(InstrumentCalendar calendar) {
        return instrumentCalendarMapper.insert(calendar) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCalendarEvent(InstrumentCalendar calendar) {
        return instrumentCalendarMapper.updateById(calendar) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCalendarEvent(Long id) {
        return instrumentCalendarMapper.deleteById(id) > 0;
    }

    @Override
    public List<Instrument> getAvailableInstrumentsByDetectItem(Long detectItemId) {
        return instrumentMapper.selectAvailableInstrumentsByDetectItem(detectItemId);
    }
}
