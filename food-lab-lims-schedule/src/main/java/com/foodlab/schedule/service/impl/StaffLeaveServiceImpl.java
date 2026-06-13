package com.foodlab.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.schedule.constant.ScheduleConstants;
import com.foodlab.schedule.entity.StaffLeave;
import com.foodlab.schedule.mapper.StaffLeaveMapper;
import com.foodlab.schedule.service.StaffLeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffLeaveServiceImpl implements StaffLeaveService {

    private final StaffLeaveMapper staffLeaveMapper;

    @Override
    public IPage<StaffLeave> getLeavePage(int pageNum, int pageSize, Long userId, String status) {
        LambdaQueryWrapper<StaffLeave> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(StaffLeave::getUserId, userId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(StaffLeave::getStatus, status);
        }
        wrapper.orderByDesc(StaffLeave::getCreateTime);
        return staffLeaveMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public StaffLeave getLeaveById(Long id) {
        return staffLeaveMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyLeave(StaffLeave leave, Long userId) {
        leave.setCreateBy(userId);
        leave.setStatus(ScheduleConstants.LEAVE_STATUS_PENDING);
        return staffLeaveMapper.insert(leave) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveLeave(Long id, Long approverId, String remark) {
        StaffLeave leave = staffLeaveMapper.selectById(id);
        if (leave == null) {
            throw new BusinessException("请假记录不存在");
        }
        if (!ScheduleConstants.LEAVE_STATUS_PENDING.equals(leave.getStatus())) {
            throw new BusinessException("只有待审批状态可以审批");
        }
        StaffLeave updated = new StaffLeave();
        updated.setId(id);
        updated.setStatus(ScheduleConstants.LEAVE_STATUS_APPROVED);
        updated.setApproverId(approverId);
        updated.setApproveTime(LocalDateTime.now());
        updated.setApproveRemark(remark);
        updated.setUpdateBy(approverId);
        return staffLeaveMapper.updateById(updated) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectLeave(Long id, Long approverId, String remark) {
        StaffLeave leave = staffLeaveMapper.selectById(id);
        if (leave == null) {
            throw new BusinessException("请假记录不存在");
        }
        if (!ScheduleConstants.LEAVE_STATUS_PENDING.equals(leave.getStatus())) {
            throw new BusinessException("只有待审批状态可以审批");
        }
        StaffLeave updated = new StaffLeave();
        updated.setId(id);
        updated.setStatus(ScheduleConstants.LEAVE_STATUS_REJECTED);
        updated.setApproverId(approverId);
        updated.setApproveTime(LocalDateTime.now());
        updated.setApproveRemark(remark);
        updated.setUpdateBy(approverId);
        return staffLeaveMapper.updateById(updated) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelLeave(Long id, Long userId) {
        StaffLeave leave = staffLeaveMapper.selectById(id);
        if (leave == null) {
            throw new BusinessException("请假记录不存在");
        }
        if (ScheduleConstants.LEAVE_STATUS_APPROVED.equals(leave.getStatus())) {
            throw new BusinessException("已批准的请假不能取消");
        }
        StaffLeave updated = new StaffLeave();
        updated.setId(id);
        updated.setStatus(ScheduleConstants.LEAVE_STATUS_CANCELLED);
        updated.setUpdateBy(userId);
        return staffLeaveMapper.updateById(updated) > 0;
    }

    @Override
    public List<StaffLeave> getLeavesByUserAndRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return staffLeaveMapper.selectLeavesByUserAndRange(userId, startDate, endDate);
    }

    @Override
    public List<Long> getUsersOnLeave(LocalDate startDate, LocalDate endDate) {
        return staffLeaveMapper.selectUsersOnLeaveByRange(startDate, endDate);
    }

    @Override
    public boolean isUserOnLeave(Long userId, LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        List<StaffLeave> leaves = staffLeaveMapper.selectLeavesByUserAndDate(userId, date);
        if (leaves.isEmpty()) {
            return false;
        }
        for (StaffLeave leave : leaves) {
            LocalTime startTime = leave.getStartTime() != null ? leave.getStartTime() : LocalTime.MIN;
            LocalTime endTime = leave.getEndTime() != null ? leave.getEndTime() : LocalTime.MAX;
            LocalDateTime leaveStart = leave.getStartDate().atTime(startTime);
            LocalDateTime leaveEnd = leave.getEndDate().atTime(endTime);
            if (!dateTime.isBefore(leaveStart) && !dateTime.isAfter(leaveEnd)) {
                return true;
            }
        }
        return false;
    }
}
