package com.foodlab.schedule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.schedule.entity.StaffLeave;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StaffLeaveMapper extends BaseMapper<StaffLeave> {

    List<StaffLeave> selectLeavesByUserAndRange(@Param("userId") Long userId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    List<StaffLeave> selectLeavesByUserAndDate(@Param("userId") Long userId,
                                                @Param("date") LocalDate date);

    List<Long> selectUsersOnLeaveByRange(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}
