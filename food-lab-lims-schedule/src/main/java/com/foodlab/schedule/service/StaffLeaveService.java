package com.foodlab.schedule.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.schedule.entity.StaffLeave;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StaffLeaveService {

    IPage<StaffLeave> getLeavePage(int pageNum, int pageSize, Long userId, String status);

    StaffLeave getLeaveById(Long id);

    boolean applyLeave(StaffLeave leave, Long userId);

    boolean approveLeave(Long id, Long approverId, String remark);

    boolean rejectLeave(Long id, Long approverId, String remark);

    boolean cancelLeave(Long id, Long userId);

    List<StaffLeave> getLeavesByUserAndRange(Long userId, LocalDate startDate, LocalDate endDate);

    List<Long> getUsersOnLeave(LocalDate startDate, LocalDate endDate);

    boolean isUserOnLeave(Long userId, LocalDateTime dateTime);
}
