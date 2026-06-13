package com.foodlab.schedule.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.schedule.dto.ScheduleAdjustDTO;
import com.foodlab.schedule.dto.ScheduleGenerateDTO;
import com.foodlab.schedule.dto.ScheduleQueryDTO;
import com.foodlab.schedule.vo.*;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    String generateSchedule(ScheduleGenerateDTO dto, Long userId);

    IPage<ScheduleResultVO> getSchedulePage(int pageNum, int pageSize, ScheduleQueryDTO queryDTO);

    List<ScheduleResultVO> getScheduleList(ScheduleQueryDTO queryDTO);

    List<ScheduleGanttVO> getGanttData(LocalDate startDate, LocalDate endDate, String groupBy);

    List<GanttResourceVO> getGanttResources(String type);

    List<ScheduleConflictVO> checkConflicts(LocalDate startDate, LocalDate endDate);

    boolean adjustSchedule(ScheduleAdjustDTO dto, Long userId);

    boolean cancelSchedule(Long id, String reason, Long userId);

    boolean publishSchedule(String batchNo, Long userId);

    List<ScheduleResultVO> getScheduleByBatchNo(String batchNo);

    ScheduleResultVO getScheduleDetail(Long id);
}
