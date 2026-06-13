package com.foodlab.schedule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.schedule.entity.ScheduleResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.schedule.dto.ScheduleQueryDTO;
import com.foodlab.schedule.vo.ScheduleResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleResultMapper extends BaseMapper<ScheduleResult> {

    IPage<ScheduleResultVO> selectScheduleList(IPage<ScheduleResultVO> page, @Param("query") ScheduleQueryDTO query);

    List<ScheduleResult> selectByBatchNo(@Param("batchNo") String batchNo);

    List<ScheduleResult> selectByInstrumentAndDate(@Param("instrumentId") Long instrumentId,
                                                    @Param("scheduleDate") LocalDate scheduleDate);

    List<ScheduleResult> selectByInstrumentAndRange(@Param("instrumentId") Long instrumentId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    List<ScheduleResult> selectByPersonAndRange(@Param("personId") Long personId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
}
