package com.foodlab.schedule.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodlab.common.constant.TaskConstants;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.detect.entity.DetectItem;
import com.foodlab.detect.mapper.DetectItemMapper;
import com.foodlab.schedule.constant.ScheduleConstants;
import com.foodlab.schedule.dto.ScheduleAdjustDTO;
import com.foodlab.schedule.dto.ScheduleGenerateDTO;
import com.foodlab.schedule.dto.ScheduleQueryDTO;
import com.foodlab.schedule.entity.*;
import com.foodlab.schedule.mapper.*;
import com.foodlab.schedule.service.ScheduleService;
import com.foodlab.schedule.vo.*;
import com.foodlab.sample.entity.Sample;
import com.foodlab.sample.entity.SampleDetectItem;
import com.foodlab.sample.mapper.SampleDetectItemMapper;
import com.foodlab.sample.mapper.SampleMapper;
import com.foodlab.system.entity.SysUser;
import com.foodlab.system.mapper.SysUserMapper;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.mapper.DetectTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleResultMapper scheduleResultMapper;
    private final ScheduleLogMapper scheduleLogMapper;
    private final InstrumentMapper instrumentMapper;
    private final DetectItemInstrumentMapper detectItemInstrumentMapper;
    private final StaffLeaveMapper staffLeaveMapper;
    private final InstrumentCalendarMapper instrumentCalendarMapper;
    private final DetectTaskMapper detectTaskMapper;
    private final DetectItemMapper detectItemMapper;
    private final SampleMapper sampleMapper;
    private final SampleDetectItemMapper sampleDetectItemMapper;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateSchedule(ScheduleGenerateDTO dto, Long userId) {
        long startTime = System.currentTimeMillis();
        String batchNo = generateBatchNo();
        log.info("开始生成排程，批次号：{}，时间范围：{} ~ {}", batchNo, dto.getStartDate(), dto.getEndDate());

        List<DetectTask> pendingTasks = getPendingTasks(dto.getTaskIds());
        if (CollUtil.isEmpty(pendingTasks)) {
            throw new BusinessException("没有待排程的任务");
        }

        List<Instrument> availableInstruments = getAvailableInstruments(dto.getInstrumentIds());
        if (CollUtil.isEmpty(availableInstruments)) {
            throw new BusinessException("没有可用的仪器");
        }

        List<SysUser> availablePersons = getAvailableDetectPersons(dto.getDetectPersonIds(), dto.getStartDate(), dto.getEndDate(), dto.getConsiderStaffLeave());
        Map<Long, Instrument> instrumentMap = availableInstruments.stream()
                .collect(Collectors.toMap(Instrument::getId, i -> i));

        List<Long> usersOnLeave = dto.getConsiderStaffLeave()
                ? staffLeaveMapper.selectUsersOnLeaveByRange(dto.getStartDate(), dto.getEndDate())
                : Collections.emptyList();
        Set<Long> leaveSet = new HashSet<>(usersOnLeave);

        Map<Long, Sample> sampleCache = new HashMap<>();
        for (DetectTask task : pendingTasks) {
            sampleCache.computeIfAbsent(task.getSampleId(), sid -> sampleMapper.selectById(sid));
        }

        pendingTasks.sort((t1, t2) -> compareTaskPriorityAndUrgency(t1, t2, sampleCache));

        Map<Long, List<LocalDateTime[]>> batchOccupiedByInstrument = new HashMap<>();

        List<ScheduleResult> allResults = new ArrayList<>();
        int sortOrder = 1;
        int conflictCount = 0;

        for (DetectTask task : pendingTasks) {
            List<SampleDetectItem> sampleDetectItems = sampleDetectItemMapper.selectList(
                    new LambdaQueryWrapper<SampleDetectItem>()
                            .eq(SampleDetectItem::getSampleId, task.getSampleId())
            );

            if (CollUtil.isEmpty(sampleDetectItems)) {
                continue;
            }

            for (SampleDetectItem sdi : sampleDetectItems) {
                DetectItem detectItem = detectItemMapper.selectById(sdi.getDetectItemId());
                if (detectItem == null) {
                    continue;
                }

                List<DetectItemInstrument> itemInstruments = detectItemInstrumentMapper
                        .selectByDetectItemId(detectItem.getId());
                if (CollUtil.isEmpty(itemInstruments)) {
                    log.warn("检测项目{}没有配置仪器，跳过：{}", detectItem.getId(), detectItem.getItemName());
                    continue;
                }

                boolean scheduled = false;
                for (DetectItemInstrument dii : itemInstruments) {
                    Instrument instrument = instrumentMap.get(dii.getInstrumentId());
                    if (instrument == null) {
                        continue;
                    }

                    int duration = dii.getEstimatedDurationMinutes();

                    TimeSlot slot = findEarliestFreeSlot(
                            instrument.getId(),
                            dto.getStartDate(),
                            dto.getEndDate(),
                            duration,
                            instrument.getDailyStartTime(),
                            instrument.getDailyEndTime(),
                            dto.getConsiderInstrumentCalendar(),
                            batchOccupiedByInstrument
                    );

                    if (slot == null) {
                        log.warn("任务{}项目{}在仪器{}上无法安排", task.getTaskCode(), detectItem.getItemName(), instrument.getInstrumentName());
                        continue;
                    }

                    SysUser assignedPerson = findAvailablePerson(
                            availablePersons,
                            leaveSet,
                            slot.start,
                            slot.end,
                            dto.getConsiderStaffLeave()
                    );

                    ScheduleResult result = new ScheduleResult();
                    result.setScheduleBatchNo(batchNo);
                    result.setTaskId(task.getId());
                    result.setTaskCode(task.getTaskCode());
                    result.setSampleId(task.getSampleId());
                    result.setSampleCode(task.getSampleCode());
                    result.setDetectItemId(detectItem.getId());
                    result.setDetectItemName(detectItem.getItemName());
                    result.setInstrumentId(instrument.getId());
                    result.setInstrumentName(instrument.getInstrumentName());
                    if (assignedPerson != null) {
                        result.setDetectPersonId(assignedPerson.getId());
                        result.setDetectPersonName(assignedPerson.getRealName());
                    }
                    result.setScheduleDate(slot.start.toLocalDate());
                    result.setStartTime(slot.start);
                    result.setEndTime(slot.end);
                    result.setDurationMinutes(duration);
                    result.setPriority(task.getPriority());
                    result.setStatus(ScheduleConstants.SCHEDULE_STATUS_SCHEDULED);
                    result.setSource(ScheduleConstants.SCHEDULE_SOURCE_AUTO);
                    result.setSortOrder(sortOrder++);
                    allResults.add(result);

                    batchOccupiedByInstrument
                            .computeIfAbsent(instrument.getId(), k -> new ArrayList<>())
                            .add(new LocalDateTime[]{slot.start, slot.end});

                    scheduled = true;
                    break;
                }

                if (!scheduled) {
                    conflictCount++;
                    log.warn("任务{}项目{}在所有仪器上均无法安排，存在冲突", task.getTaskCode(), detectItem.getItemName());
                }
            }
        }

        for (ScheduleResult result : allResults) {
            scheduleResultMapper.insert(result);
        }

        long timeCost = System.currentTimeMillis() - startTime;

        ScheduleLog scheduleLog = new ScheduleLog();
        scheduleLog.setScheduleBatchNo(batchNo);
        scheduleLog.setOperationType(ScheduleConstants.OPERATION_TYPE_GENERATE);
        scheduleLog.setTaskCount(pendingTasks.size());
        scheduleLog.setConflictCount(conflictCount);
        scheduleLog.setAlgorithmType(dto.getAlgorithmType());
        scheduleLog.setTimeCostMs(timeCost);
        scheduleLog.setRemark("自动排程完成，共生成" + allResults.size() + "条排程记录");
        scheduleLog.setCreateBy(userId);
        scheduleLogMapper.insert(scheduleLog);

        log.info("排程生成完成，批次号：{}，耗时：{}ms，记录数：{}，冲突数：{}", batchNo, timeCost, allResults.size(), conflictCount);

        if (Boolean.TRUE.equals(dto.getAutoPublish())) {
            publishSchedule(batchNo, userId);
        }

        return batchNo;
    }

    private TimeSlot findEarliestFreeSlot(Long instrumentId, LocalDate startDate, LocalDate endDate,
                                          int durationMinutes, LocalTime dailyStart, LocalTime dailyEnd,
                                          boolean considerCalendar,
                                          Map<Long, List<LocalDateTime[]>> batchOccupiedByInstrument) {
        LocalDate currentDate = startDate;
        int maxDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        for (int day = 0; day < maxDays && !currentDate.isAfter(endDate); day++) {
            DayOfWeek dow = currentDate.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            LocalDateTime dayStart = currentDate.atTime(dailyStart);
            LocalDateTime dayEnd = currentDate.atTime(dailyEnd);
            long dayDurationMinutes = ChronoUnit.MINUTES.between(dayStart, dayEnd);
            if (dayDurationMinutes < durationMinutes) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            List<LocalDateTime[]> occupiedIntervals = new ArrayList<>();

            if (considerCalendar) {
                List<InstrumentCalendar> calendars = instrumentMapper.selectOccupiedSlots(instrumentId, currentDate);
                for (InstrumentCalendar cal : calendars) {
                    LocalDateTime s = currentDate.atTime(cal.getStartTime());
                    LocalDateTime e = currentDate.atTime(cal.getEndTime());
                    occupiedIntervals.add(new LocalDateTime[]{s, e});
                }

                List<ScheduleResult> existingSchedules = scheduleResultMapper.selectByInstrumentAndDate(instrumentId, currentDate);
                for (ScheduleResult sr : existingSchedules) {
                    occupiedIntervals.add(new LocalDateTime[]{sr.getStartTime(), sr.getEndTime()});
                }
            }

            List<LocalDateTime[]> batchIntervals = batchOccupiedByInstrument.getOrDefault(instrumentId, Collections.emptyList());
            for (LocalDateTime[] bi : batchIntervals) {
                if (!bi[0].toLocalDate().isAfter(currentDate) && !bi[1].toLocalDate().isBefore(currentDate)) {
                    occupiedIntervals.add(bi);
                }
            }

            occupiedIntervals.sort(Comparator.comparing(a -> a[0]));

            LocalDateTime candidateStart = dayStart;
            boolean found = false;

            for (LocalDateTime[] interval : occupiedIntervals) {
                LocalDateTime slotEnd = candidateStart.plusMinutes(durationMinutes);
                if (slotEnd.isAfter(dayEnd)) {
                    break;
                }
                if (candidateStart.plusMinutes(durationMinutes).isBefore(interval[0])
                        || candidateStart.plusMinutes(durationMinutes).equals(interval[0])) {
                    return new TimeSlot(candidateStart, candidateStart.plusMinutes(durationMinutes));
                }
                if (!interval[1].isAfter(candidateStart)) {
                    continue;
                }
                candidateStart = interval[1].isAfter(candidateStart) ? interval[1] : candidateStart;
            }

            if (!found) {
                LocalDateTime finalEnd = candidateStart.plusMinutes(durationMinutes);
                if (!finalEnd.isAfter(dayEnd) && candidateStart.isBefore(dayEnd)) {
                    return new TimeSlot(candidateStart, finalEnd);
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        return null;
    }

    private SysUser findAvailablePerson(List<SysUser> persons, Set<Long> leaveSet,
                                       LocalDateTime start, LocalDateTime end,
                                       boolean considerLeave) {
        if (CollUtil.isEmpty(persons)) {
            return null;
        }

        for (SysUser person : persons) {
            if (!considerLeave) {
                return person;
            }
            if (leaveSet.contains(person.getId())) {
                continue;
            }
            List<StaffLeave> leaves = staffLeaveMapper.selectLeavesByUserAndRange(
                    person.getId(), start.toLocalDate(), end.toLocalDate()
            );
            if (CollUtil.isEmpty(leaves)) {
                return person;
            }
            boolean hasConflict = false;
            for (StaffLeave leave : leaves) {
                LocalDateTime leaveStart = leave.getStartDate().atTime(
                        leave.getStartTime() != null ? leave.getStartTime() : LocalTime.MIN
                );
                LocalDateTime leaveEnd = leave.getEndDate().atTime(
                        leave.getEndTime() != null ? leave.getEndTime() : LocalTime.MAX
                );
                if (start.isBefore(leaveEnd) && end.isAfter(leaveStart)) {
                    hasConflict = true;
                    break;
                }
            }
            if (!hasConflict) {
                return person;
            }
        }

        return persons.get(0);
    }

    private int compareTaskPriorityAndUrgency(DetectTask t1, DetectTask t2, Map<Long, Sample> sampleCache) {
        int w1 = getPriorityWeight(t1.getPriority());
        int w2 = getPriorityWeight(t2.getPriority());

        Sample s1 = sampleCache.get(t1.getSampleId());
        Sample s2 = sampleCache.get(t2.getSampleId());
        int u1 = getUrgencyWeight(t1, s1);
        int u2 = getUrgencyWeight(t2, s2);

        int score1 = w1 + u1;
        int score2 = w2 + u2;

        if (score1 != score2) {
            return Integer.compare(score2, score1);
        }
        return t1.getId().compareTo(t2.getId());
    }

    private int getPriorityWeight(String priority) {
        if (TaskConstants.TASK_PRIORITY_HIGH.equals(priority)) {
            return ScheduleConstants.PRIORITY_WEIGHT_HIGH;
        } else if (TaskConstants.TASK_PRIORITY_MEDIUM.equals(priority)) {
            return ScheduleConstants.PRIORITY_WEIGHT_MEDIUM;
        }
        return ScheduleConstants.PRIORITY_WEIGHT_LOW;
    }

    private int getUrgencyWeight(DetectTask task, Sample sample) {
        if (TaskConstants.TASK_TYPE_URGENT.equals(task.getTaskType())) {
            return ScheduleConstants.URGENCY_WEIGHT_URGENT;
        }
        return ScheduleConstants.URGENCY_WEIGHT_NORMAL;
    }

    private List<DetectTask> getPendingTasks(List<Long> taskIds) {
        LambdaQueryWrapper<DetectTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DetectTask::getTaskStatus, TaskConstants.TASK_STATUS_PENDING, TaskConstants.TASK_STATUS_ASSIGNED)
                .orderByAsc(DetectTask::getCreateTime);
        if (CollUtil.isNotEmpty(taskIds)) {
            wrapper.in(DetectTask::getId, taskIds);
        }
        return detectTaskMapper.selectList(wrapper);
    }

    private List<Instrument> getAvailableInstruments(List<Long> instrumentIds) {
        LambdaQueryWrapper<Instrument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Instrument::getStatus, ScheduleConstants.INSTRUMENT_STATUS_AVAILABLE);
        if (CollUtil.isNotEmpty(instrumentIds)) {
            wrapper.in(Instrument::getId, instrumentIds);
        }
        return instrumentMapper.selectList(wrapper);
    }

    private List<SysUser> getAvailableDetectPersons(List<Long> detectPersonIds, LocalDate start, LocalDate end, boolean considerLeave) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getStatus, 1);
        if (CollUtil.isNotEmpty(detectPersonIds)) {
            wrapper.in(SysUser::getId, detectPersonIds);
        }
        return sysUserMapper.selectList(wrapper);
    }

    private String generateBatchNo() {
        return ScheduleConstants.BATCH_NO_PREFIX + DateUtil.format(new Date(), "yyyyMMddHHmmss") +
                String.format("%04d", IdUtil.getSnowflakeNextId() % 10000);
    }

    @Override
    public IPage<ScheduleResultVO> getSchedulePage(int pageNum, int pageSize, ScheduleQueryDTO queryDTO) {
        Page<ScheduleResultVO> page = new Page<>(pageNum, pageSize);
        return scheduleResultMapper.selectScheduleList(page, queryDTO);
    }

    @Override
    public List<ScheduleResultVO> getScheduleList(ScheduleQueryDTO queryDTO) {
        return scheduleResultMapper.selectScheduleList(new Page<>(1, Integer.MAX_VALUE), queryDTO).getRecords();
    }

    @Override
    public List<ScheduleGanttVO> getGanttData(LocalDate startDate, LocalDate endDate, String groupBy) {
        ScheduleQueryDTO query = new ScheduleQueryDTO();
        query.setScheduleDateStart(startDate);
        query.setScheduleDateEnd(endDate);
        query.setStatusList(Arrays.asList(
                ScheduleConstants.SCHEDULE_STATUS_SCHEDULED,
                ScheduleConstants.SCHEDULE_STATUS_IN_PROGRESS,
                ScheduleConstants.SCHEDULE_STATUS_COMPLETED
        ));

        List<ScheduleResultVO> list = getScheduleList(query);

        return list.stream().map(sr -> {
            ScheduleGanttVO gantt = new ScheduleGanttVO();
            gantt.setId(sr.getId());
            gantt.setTaskId(String.valueOf(sr.getTaskId()));
            gantt.setName(sr.getTaskCode() + " - " + sr.getDetectItemName());
            gantt.setInstrumentId(sr.getInstrumentId());
            gantt.setInstrumentName(sr.getInstrumentName());
            gantt.setDetectPersonId(sr.getDetectPersonId());
            gantt.setDetectPersonName(sr.getDetectPersonName());
            gantt.setStart(sr.getStartTime());
            gantt.setEnd(sr.getEndTime());
            gantt.setDuration(sr.getDurationMinutes());
            gantt.setPriority(sr.getPriority());
            gantt.setStatus(sr.getStatus());
            gantt.setSource(sr.getSource());
            gantt.setScheduleBatchNo(sr.getScheduleBatchNo());
            gantt.setRemark(sr.getRemark());
            gantt.setColor(getPriorityColor(sr.getPriority()));
            gantt.setCanDrag(ScheduleConstants.SCHEDULE_STATUS_SCHEDULED.equals(sr.getStatus()));
            gantt.setCanResize(ScheduleConstants.SCHEDULE_STATUS_SCHEDULED.equals(sr.getStatus()));
            return gantt;
        }).collect(Collectors.toList());
    }

    private String getPriorityColor(String priority) {
        if (TaskConstants.TASK_PRIORITY_HIGH.equals(priority)) {
            return "#ef4444";
        } else if (TaskConstants.TASK_PRIORITY_LOW.equals(priority)) {
            return "#22c55e";
        }
        return "#3b82f6";
    }

    @Override
    public List<GanttResourceVO> getGanttResources(String type) {
        List<GanttResourceVO> result = new ArrayList<>();

        if ("instrument".equalsIgnoreCase(type) || "all".equalsIgnoreCase(type)) {
            List<Instrument> instruments = instrumentMapper.selectList(null);
            for (Instrument ins : instruments) {
                GanttResourceVO vo = new GanttResourceVO();
                vo.setId(ins.getId());
                vo.setName(ins.getInstrumentName());
                vo.setType("instrument");
                vo.setCode(ins.getInstrumentCode());
                vo.setStatus(ins.getStatus());
                vo.setLocation(ins.getLocation());
                result.add(vo);
            }
        }

        if ("person".equalsIgnoreCase(type) || "all".equalsIgnoreCase(type)) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getStatus, 1);
            List<SysUser> users = sysUserMapper.selectList(wrapper);
            for (SysUser u : users) {
                GanttResourceVO vo = new GanttResourceVO();
                vo.setId(u.getId() + 1000000L);
                vo.setName(u.getRealName());
                vo.setType("person");
                vo.setCode(u.getUsername());
                result.add(vo);
            }
        }

        return result;
    }

    @Override
    public List<ScheduleConflictVO> checkConflicts(LocalDate startDate, LocalDate endDate) {
        List<ScheduleConflictVO> conflicts = new ArrayList<>();
        long conflictId = 1L;

        ScheduleQueryDTO query = new ScheduleQueryDTO();
        query.setScheduleDateStart(startDate);
        query.setScheduleDateEnd(endDate);
        List<ScheduleResultVO> schedules = getScheduleList(query);

        Map<Long, List<ScheduleResultVO>> byInstrument = schedules.stream()
                .collect(Collectors.groupingBy(ScheduleResultVO::getInstrumentId));

        for (Map.Entry<Long, List<ScheduleResultVO>> entry : byInstrument.entrySet()) {
            List<ScheduleResultVO> list = entry.getValue();
            list.sort(Comparator.comparing(ScheduleResultVO::getStartTime));
            for (int i = 0; i < list.size() - 1; i++) {
                ScheduleResultVO curr = list.get(i);
                ScheduleResultVO next = list.get(i + 1);
                if (curr.getEndTime().isAfter(next.getStartTime())) {
                    ScheduleConflictVO c = new ScheduleConflictVO();
                    c.setId(conflictId++);
                    c.setConflictType("INSTRUMENT_CONFLICT");
                    c.setConflictDescription("仪器时间冲突");
                    c.setScheduleId1(curr.getId());
                    c.setScheduleId2(next.getId());
                    c.setTaskName1(curr.getTaskCode() + "-" + curr.getDetectItemName());
                    c.setTaskName2(next.getTaskCode() + "-" + next.getDetectItemName());
                    c.setResourceId(curr.getInstrumentId());
                    c.setResourceName(curr.getInstrumentName());
                    c.setResourceType("INSTRUMENT");
                    c.setConflictStart(next.getStartTime());
                    LocalDateTime conflictEnd = curr.getEndTime().isBefore(next.getEndTime()) ? curr.getEndTime() : next.getEndTime();
                    c.setConflictEnd(conflictEnd);
                    c.setSeverity("HIGH");
                    conflicts.add(c);
                }
            }
        }

        Map<Long, List<ScheduleResultVO>> byPerson = schedules.stream()
                .filter(s -> s.getDetectPersonId() != null)
                .collect(Collectors.groupingBy(ScheduleResultVO::getDetectPersonId));

        for (Map.Entry<Long, List<ScheduleResultVO>> entry : byPerson.entrySet()) {
            List<ScheduleResultVO> list = entry.getValue();
            list.sort(Comparator.comparing(ScheduleResultVO::getStartTime));
            for (int i = 0; i < list.size() - 1; i++) {
                ScheduleResultVO curr = list.get(i);
                ScheduleResultVO next = list.get(i + 1);
                if (curr.getEndTime().isAfter(next.getStartTime())) {
                    ScheduleConflictVO c = new ScheduleConflictVO();
                    c.setId(conflictId++);
                    c.setConflictType("PERSON_CONFLICT");
                    c.setConflictDescription("人员时间冲突");
                    c.setScheduleId1(curr.getId());
                    c.setScheduleId2(next.getId());
                    c.setTaskName1(curr.getTaskCode() + "-" + curr.getDetectItemName());
                    c.setTaskName2(next.getTaskCode() + "-" + next.getDetectItemName());
                    c.setResourceId(curr.getDetectPersonId());
                    c.setResourceName(curr.getDetectPersonName());
                    c.setResourceType("PERSON");
                    c.setConflictStart(next.getStartTime());
                    LocalDateTime personConflictEnd = curr.getEndTime().isBefore(next.getEndTime()) ? curr.getEndTime() : next.getEndTime();
                    c.setConflictEnd(personConflictEnd);
                    c.setSeverity("MEDIUM");
                    conflicts.add(c);
                }
            }
        }

        return conflicts;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustSchedule(ScheduleAdjustDTO dto, Long userId) {
        ScheduleResult sr = scheduleResultMapper.selectById(dto.getId());
        if (sr == null) {
            throw new BusinessException("排程记录不存在");
        }
        if (!ScheduleConstants.SCHEDULE_STATUS_SCHEDULED.equals(sr.getStatus())
                && !ScheduleConstants.SCHEDULE_STATUS_ADJUSTED.equals(sr.getStatus())) {
            throw new BusinessException("只有已排程或已调整状态可以调整");
        }

        Long targetInstrumentId = dto.getNewInstrumentId() != null ? dto.getNewInstrumentId() : sr.getInstrumentId();
        Long targetPersonId = dto.getNewDetectPersonId() != null ? dto.getNewDetectPersonId() : sr.getDetectPersonId();

        List<ScheduleResult> instrumentSchedules = scheduleResultMapper.selectByInstrumentAndRange(
                targetInstrumentId,
                dto.getNewStartTime().toLocalDate(),
                dto.getNewEndTime().toLocalDate()
        );

        for (ScheduleResult existing : instrumentSchedules) {
            if (existing.getId().equals(dto.getId())) {
                continue;
            }
            if (dto.getNewStartTime().isBefore(existing.getEndTime())
                    && dto.getNewEndTime().isAfter(existing.getStartTime())) {
                throw new BusinessException(
                        String.format("目标仪器在 %s ~ %s 已有排程冲突（任务：%s）",
                                existing.getStartTime(), existing.getEndTime(), existing.getTaskCode()));
            }
        }

        if (targetPersonId != null) {
            List<ScheduleResult> personSchedules = scheduleResultMapper.selectByPersonAndRange(
                    targetPersonId,
                    dto.getNewStartTime().toLocalDate(),
                    dto.getNewEndTime().toLocalDate()
            );
            for (ScheduleResult existing : personSchedules) {
                if (existing.getId().equals(dto.getId())) {
                    continue;
                }
                if (dto.getNewStartTime().isBefore(existing.getEndTime())
                        && dto.getNewEndTime().isAfter(existing.getStartTime())) {
                    throw new BusinessException(
                            String.format("目标人员在 %s ~ %s 已有排程冲突（任务：%s）",
                                    existing.getStartTime(), existing.getEndTime(), existing.getTaskCode()));
                }
            }
        }

        ScheduleResult adjusted = new ScheduleResult();
        adjusted.setId(sr.getId());
        adjusted.setStartTime(dto.getNewStartTime());
        adjusted.setEndTime(dto.getNewEndTime());
        adjusted.setScheduleDate(dto.getNewStartTime().toLocalDate());
        adjusted.setDurationMinutes((int) ChronoUnit.MINUTES.between(dto.getNewStartTime(), dto.getNewEndTime()));
        adjusted.setStatus(ScheduleConstants.SCHEDULE_STATUS_ADJUSTED);
        adjusted.setSource(ScheduleConstants.SCHEDULE_SOURCE_MANUAL);
        if (dto.getNewInstrumentId() != null) {
            Instrument ins = instrumentMapper.selectById(dto.getNewInstrumentId());
            if (ins != null) {
                adjusted.setInstrumentId(ins.getId());
                adjusted.setInstrumentName(ins.getInstrumentName());
            }
        }
        if (dto.getNewDetectPersonId() != null) {
            SysUser user = sysUserMapper.selectById(dto.getNewDetectPersonId());
            if (user != null) {
                adjusted.setDetectPersonId(user.getId());
                adjusted.setDetectPersonName(user.getRealName());
            }
        }
        adjusted.setRemark(dto.getReason());
        adjusted.setUpdateBy(userId);
        boolean result = scheduleResultMapper.updateById(adjusted) > 0;

        ScheduleLog log = new ScheduleLog();
        log.setScheduleBatchNo(sr.getScheduleBatchNo());
        log.setOperationType(ScheduleConstants.OPERATION_TYPE_ADJUST);
        log.setRemark("手动调整排程：" + dto.getReason());
        log.setCreateBy(userId);
        scheduleLogMapper.insert(log);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelSchedule(Long id, String reason, Long userId) {
        ScheduleResult sr = scheduleResultMapper.selectById(id);
        if (sr == null) {
            throw new BusinessException("排程记录不存在");
        }

        ScheduleResult updated = new ScheduleResult();
        updated.setId(id);
        updated.setStatus(ScheduleConstants.SCHEDULE_STATUS_CANCELLED);
        updated.setRemark(reason);
        updated.setUpdateBy(userId);
        boolean result = scheduleResultMapper.updateById(updated) > 0;

        ScheduleLog log = new ScheduleLog();
        log.setScheduleBatchNo(sr.getScheduleBatchNo());
        log.setOperationType(ScheduleConstants.OPERATION_TYPE_CANCEL);
        log.setRemark("取消排程：" + reason);
        log.setCreateBy(userId);
        scheduleLogMapper.insert(log);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishSchedule(String batchNo, Long userId) {
        List<ScheduleResult> results = scheduleResultMapper.selectByBatchNo(batchNo);
        if (CollUtil.isEmpty(results)) {
            throw new BusinessException("排程批次不存在或无记录");
        }

        for (ScheduleResult sr : results) {
            InstrumentCalendar cal = new InstrumentCalendar();
            cal.setInstrumentId(sr.getInstrumentId());
            cal.setCalendarDate(sr.getScheduleDate());
            cal.setStartTime(sr.getStartTime().toLocalTime());
            cal.setEndTime(sr.getEndTime().toLocalTime());
            cal.setEventType(ScheduleConstants.CALENDAR_EVENT_OCCUPIED);
            cal.setTaskId(sr.getTaskId());
            cal.setTitle(sr.getTaskCode() + " - " + sr.getDetectItemName());
            cal.setCreateBy(userId);
            instrumentCalendarMapper.insert(cal);
        }

        ScheduleLog log = new ScheduleLog();
        log.setScheduleBatchNo(batchNo);
        log.setOperationType(ScheduleConstants.OPERATION_TYPE_PUBLISH);
        log.setTaskCount(results.size());
        log.setRemark("发布排程，同步到仪器日历");
        log.setCreateBy(userId);
        scheduleLogMapper.insert(log);

        return true;
    }

    @Override
    public List<ScheduleResultVO> getScheduleByBatchNo(String batchNo) {
        List<ScheduleResult> list = scheduleResultMapper.selectByBatchNo(batchNo);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public ScheduleResultVO getScheduleDetail(Long id) {
        ScheduleResult sr = scheduleResultMapper.selectById(id);
        return sr != null ? convertToVO(sr) : null;
    }

    private ScheduleResultVO convertToVO(ScheduleResult sr) {
        ScheduleResultVO vo = new ScheduleResultVO();
        BeanUtils.copyProperties(sr, vo);
        return vo;
    }

    private static class TimeSlot {
        LocalDateTime start;
        LocalDateTime end;

        TimeSlot(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
