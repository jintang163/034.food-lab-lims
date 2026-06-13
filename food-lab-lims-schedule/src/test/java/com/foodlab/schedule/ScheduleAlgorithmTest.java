package com.foodlab.schedule;

import cn.hutool.core.collection.CollUtil;
import com.foodlab.common.constant.TaskConstants;
import com.foodlab.detect.entity.DetectItem;
import com.foodlab.detect.mapper.DetectItemMapper;
import com.foodlab.sample.entity.Sample;
import com.foodlab.sample.entity.SampleDetectItem;
import com.foodlab.sample.mapper.SampleDetectItemMapper;
import com.foodlab.sample.mapper.SampleMapper;
import com.foodlab.schedule.constant.ScheduleConstants;
import com.foodlab.schedule.dto.ScheduleGenerateDTO;
import com.foodlab.schedule.entity.*;
import com.foodlab.schedule.mapper.*;
import com.foodlab.schedule.service.ScheduleService;
import com.foodlab.schedule.vo.ScheduleConflictVO;
import com.foodlab.schedule.vo.ScheduleResultVO;
import com.foodlab.system.entity.SysUser;
import com.foodlab.system.mapper.SysUserMapper;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.mapper.DetectTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
@Rollback
class ScheduleAlgorithmTest {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private InstrumentMapper instrumentMapper;
    @Autowired
    private DetectItemInstrumentMapper detectItemInstrumentMapper;
    @Autowired
    private DetectTaskMapper detectTaskMapper;
    @Autowired
    private SampleMapper sampleMapper;
    @Autowired
    private SampleDetectItemMapper sampleDetectItemMapper;
    @Autowired
    private DetectItemMapper detectItemMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private StaffLeaveMapper staffLeaveMapper;
    @Autowired
    private InstrumentCalendarMapper instrumentCalendarMapper;
    @Autowired
    private ScheduleResultMapper scheduleResultMapper;

    private Instrument instrument1;
    private Instrument instrument2;
    private DetectItem detectItem1;
    private DetectItem detectItem2;
    private Sample sample1;
    private Sample sample2;
    private DetectTask taskHigh;
    private DetectTask taskLow;
    private SysUser user1;
    private SysUser user2;

    @BeforeEach
    void setUp() {
        log.info("==================== 初始化排程测试数据 ====================");

        instrument1 = new Instrument();
        instrument1.setInstrumentCode("TEST_INS_001");
        instrument1.setInstrumentName("测试仪器A-液相色谱");
        instrument1.setInstrumentType("CHROMATOGRAPHY");
        instrument1.setStatus(ScheduleConstants.INSTRUMENT_STATUS_AVAILABLE);
        instrument1.setDailyStartTime(LocalTime.of(8, 0));
        instrument1.setDailyEndTime(LocalTime.of(18, 0));
        instrument1.setLocation("测试实验室");
        instrumentMapper.insert(instrument1);
        log.info("创建仪器1: {}", instrument1.getId());

        instrument2 = new Instrument();
        instrument2.setInstrumentCode("TEST_INS_002");
        instrument2.setInstrumentName("测试仪器B-光谱仪");
        instrument2.setInstrumentType("SPECTROSCOPY");
        instrument2.setStatus(ScheduleConstants.INSTRUMENT_STATUS_AVAILABLE);
        instrument2.setDailyStartTime(LocalTime.of(8, 0));
        instrument2.setDailyEndTime(LocalTime.of(18, 0));
        instrument2.setLocation("测试实验室");
        instrumentMapper.insert(instrument2);
        log.info("创建仪器2: {}", instrument2.getId());

        detectItem1 = new DetectItem();
        detectItem1.setItemCode("TEST_ITEM_001");
        detectItem1.setItemName("蛋白质检测");
        detectItem1.setUnit("g/100g");
        detectItem1.setPrecisionValue(new BigDecimal("0.01"));
        detectItem1.setStatus("1");
        detectItemMapper.insert(detectItem1);
        log.info("创建检测项目1: {}", detectItem1.getId());

        detectItem2 = new DetectItem();
        detectItem2.setItemCode("TEST_ITEM_002");
        detectItem2.setItemName("脂肪检测");
        detectItem2.setUnit("g/100g");
        detectItem2.setPrecisionValue(new BigDecimal("0.01"));
        detectItem2.setStatus("1");
        detectItemMapper.insert(detectItem2);
        log.info("创建检测项目2: {}", detectItem2.getId());

        DetectItemInstrument dii1 = new DetectItemInstrument();
        dii1.setDetectItemId(detectItem1.getId());
        dii1.setInstrumentId(instrument1.getId());
        dii1.setEstimatedDurationMinutes(30);
        dii1.setSortOrder(1);
        dii1.setIsRequired(1);
        detectItemInstrumentMapper.insert(dii1);

        DetectItemInstrument dii2 = new DetectItemInstrument();
        dii2.setDetectItemId(detectItem2.getId());
        dii2.setInstrumentId(instrument2.getId());
        dii2.setEstimatedDurationMinutes(20);
        dii2.setSortOrder(1);
        dii2.setIsRequired(1);
        detectItemInstrumentMapper.insert(dii2);

        sample1 = new Sample();
        sample1.setSampleCode("TEST_SP_001");
        sample1.setSampleName("牛奶样品A");
        sample1.setSampleStatus("PENDING");
        sample1.setDetectItemCount(1);
        sampleMapper.insert(sample1);
        log.info("创建样品1: {}", sample1.getId());

        sample2 = new Sample();
        sample2.setSampleCode("TEST_SP_002");
        sample2.setSampleName("奶粉样品B");
        sample2.setSampleStatus("PENDING");
        sample2.setDetectItemCount(1);
        sampleMapper.insert(sample2);
        log.info("创建样品2: {}", sample2.getId());

        SampleDetectItem sdi1 = new SampleDetectItem();
        sdi1.setSampleId(sample1.getId());
        sdi1.setSampleCode(sample1.getSampleCode());
        sdi1.setDetectItemId(detectItem1.getId());
        sdi1.setDetectItemName(detectItem1.getItemName());
        sdi1.setSort(1);
        sampleDetectItemMapper.insert(sdi1);

        SampleDetectItem sdi2 = new SampleDetectItem();
        sdi2.setSampleId(sample2.getId());
        sdi2.setSampleCode(sample2.getSampleCode());
        sdi2.setDetectItemId(detectItem2.getId());
        sdi2.setDetectItemName(detectItem2.getItemName());
        sdi2.setSort(1);
        sampleDetectItemMapper.insert(sdi2);

        taskHigh = new DetectTask();
        taskHigh.setTaskCode("TEST_TASK_001");
        taskHigh.setSampleId(sample1.getId());
        taskHigh.setSampleCode(sample1.getSampleCode());
        taskHigh.setTaskName("牛奶-蛋白质检测");
        taskHigh.setPriority(TaskConstants.TASK_PRIORITY_HIGH);
        taskHigh.setTaskStatus(TaskConstants.TASK_STATUS_PENDING);
        taskHigh.setDetectItemCount(1);
        taskHigh.setCompletedItemCount(0);
        detectTaskMapper.insert(taskHigh);
        log.info("创建高优先级任务: {} - priority={}", taskHigh.getId(), taskHigh.getPriority());

        taskLow = new DetectTask();
        taskLow.setTaskCode("TEST_TASK_002");
        taskLow.setSampleId(sample2.getId());
        taskLow.setSampleCode(sample2.getSampleCode());
        taskLow.setTaskName("奶粉-脂肪检测");
        taskLow.setPriority(TaskConstants.TASK_PRIORITY_LOW);
        taskLow.setTaskStatus(TaskConstants.TASK_STATUS_PENDING);
        taskLow.setDetectItemCount(1);
        taskLow.setCompletedItemCount(0);
        detectTaskMapper.insert(taskLow);
        log.info("创建低优先级任务: {} - priority={}", taskLow.getId(), taskLow.getPriority());

        user1 = new SysUser();
        user1.setUsername("test_tester_01");
        user1.setRealName("测试员甲");
        user1.setStatus(1);
        sysUserMapper.insert(user1);

        user2 = new SysUser();
        user2.setUsername("test_tester_02");
        user2.setRealName("测试员乙");
        user2.setStatus(1);
        sysUserMapper.insert(user2);

        log.info("==================== 测试数据初始化完成 ====================");
    }

    @Test
    void testGreedyScheduleBasic() {
        log.info("==================== 测试1：基础贪心排程 ====================");
        ScheduleGenerateDTO dto = new ScheduleGenerateDTO();
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(5));
        dto.setAlgorithmType(ScheduleConstants.ALGORITHM_GREEDY);
        dto.setConsiderStaffLeave(false);
        dto.setConsiderInstrumentCalendar(false);

        String batchNo = scheduleService.generateSchedule(dto, 1L);
        assertNotNull(batchNo, "批次号不能为空");
        assertTrue(batchNo.startsWith(ScheduleConstants.BATCH_NO_PREFIX), "批次号前缀正确");
        log.info("排程生成成功，批次号：{}", batchNo);

        List<ScheduleResultVO> results = scheduleService.getScheduleByBatchNo(batchNo);
        assertTrue(CollUtil.isNotEmpty(results), "排程结果不能为空");
        log.info("共生成 {} 条排程记录", results.size());

        for (ScheduleResultVO r : results) {
            assertNotNull(r.getStartTime());
            assertNotNull(r.getEndTime());
            assertTrue(r.getEndTime().isAfter(r.getStartTime()), "结束时间必须晚于开始时间");
            assertNotNull(r.getInstrumentId());
            log.info("排程记录: task={}, instrument={}, start={}, end={}, priority={}",
                    r.getTaskCode(), r.getInstrumentName(), r.getStartTime(), r.getEndTime(), r.getPriority());
        }

        List<ScheduleResultVO> highPriorityTasks = results.stream()
                .filter(r -> TaskConstants.TASK_PRIORITY_HIGH.equals(r.getPriority()))
                .toList();
        List<ScheduleResultVO> lowPriorityTasks = results.stream()
                .filter(r -> TaskConstants.TASK_PRIORITY_LOW.equals(r.getPriority()))
                .toList();
        assertTrue(highPriorityTasks.size() > 0, "应包含高优先级任务");
        assertTrue(lowPriorityTasks.size() > 0, "应包含低优先级任务");

        LocalDateTime firstHighStart = highPriorityTasks.get(0).getStartTime();
        LocalDateTime firstLowStart = lowPriorityTasks.get(0).getStartTime();
        assertTrue(!firstHighStart.isAfter(firstLowStart),
                "贪心算法应保证高优先级任务先排：highStart=" + firstHighStart + " <= lowStart=" + firstLowStart);
        log.info("优先级验证通过：高优先级开始时间 {} 不晚于 低优先级 {}", firstHighStart, firstLowStart);

        log.info("==================== 测试1通过 ====================");
    }

    @Test
    void testScheduleExcludeWeekends() {
        log.info("==================== 测试2：周末排除验证 ====================");

        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate friday = monday.plusDays(6);
        log.info("排程范围：{} ~ {} (含周末)", monday, friday);

        ScheduleGenerateDTO dto = new ScheduleGenerateDTO();
        dto.setStartDate(monday);
        dto.setEndDate(friday);
        dto.setAlgorithmType(ScheduleConstants.ALGORITHM_GREEDY);
        dto.setConsiderStaffLeave(false);
        dto.setConsiderInstrumentCalendar(false);

        String batchNo = scheduleService.generateSchedule(dto, 1L);
        List<ScheduleResultVO> results = scheduleService.getScheduleByBatchNo(batchNo);

        for (ScheduleResultVO r : results) {
            java.time.DayOfWeek dow = r.getScheduleDate().getDayOfWeek();
            assertNotEquals(java.time.DayOfWeek.SATURDAY, dow, "不能排在周六: " + r.getScheduleDate());
            assertNotEquals(java.time.DayOfWeek.SUNDAY, dow, "不能排在周日: " + r.getScheduleDate());
            log.info("排程日期 {} - {}，验证通过（非周末）", r.getScheduleDate(), dow);
        }

        log.info("==================== 测试2通过 ====================");
    }

    @Test
    void testInstrumentCalendarConflictAvoidance() {
        log.info("==================== 测试3：仪器日历冲突规避 ====================");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        while (tomorrow.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || tomorrow.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            tomorrow = tomorrow.plusDays(1);
        }

        InstrumentCalendar block = new InstrumentCalendar();
        block.setInstrumentId(instrument1.getId());
        block.setCalendarDate(tomorrow);
        block.setStartTime(LocalTime.of(8, 0));
        block.setEndTime(LocalTime.of(12, 0));
        block.setEventType(ScheduleConstants.CALENDAR_EVENT_MAINTENANCE);
        block.setTitle("预防性维护");
        instrumentCalendarMapper.insert(block);
        log.info("创建仪器占用：仪器{} 明天 08:00-12:00 维护", instrument1.getId());

        ScheduleGenerateDTO dto = new ScheduleGenerateDTO();
        dto.setStartDate(tomorrow);
        dto.setEndDate(tomorrow.plusDays(2));
        dto.setAlgorithmType(ScheduleConstants.ALGORITHM_GREEDY);
        dto.setConsiderStaffLeave(false);
        dto.setConsiderInstrumentCalendar(true);

        String batchNo = scheduleService.generateSchedule(dto, 1L);
        List<ScheduleResultVO> results = scheduleService.getScheduleByBatchNo(batchNo);

        for (ScheduleResultVO r : results) {
            if (r.getInstrumentId().equals(instrument1.getId())
                    && r.getScheduleDate().equals(tomorrow)) {
                LocalTime taskStart = r.getStartTime().toLocalTime();
                assertTrue(taskStart.compareTo(LocalTime.of(12, 0)) >= 0,
                        "项目1在仪器1上的排程应避开08-12点，实际开始：" + taskStart);
                log.info("冲突规避验证通过：项目1在仪器1上的开始时间 {} 在12:00之后", taskStart);
            }
        }

        log.info("==================== 测试3通过 ====================");
    }

    @Test
    void testStaffLeaveAvoidance() {
        log.info("==================== 测试4：人员休假规避 ====================");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        while (tomorrow.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || tomorrow.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            tomorrow = tomorrow.plusDays(1);
        }

        StaffLeave leave = new StaffLeave();
        leave.setUserId(user1.getId());
        leave.setLeaveType(ScheduleConstants.LEAVE_TYPE_ANNUAL);
        leave.setStartDate(tomorrow);
        leave.setEndDate(tomorrow);
        leave.setStatus(ScheduleConstants.LEAVE_STATUS_APPROVED);
        leave.setReason("年假测试");
        staffLeaveMapper.insert(leave);
        log.info("创建休假：用户{} 在 {} 休年假", user1.getId(), tomorrow);

        ScheduleGenerateDTO dto = new ScheduleGenerateDTO();
        dto.setStartDate(tomorrow);
        dto.setEndDate(tomorrow.plusDays(2));
        dto.setAlgorithmType(ScheduleConstants.ALGORITHM_GREEDY);
        dto.setConsiderStaffLeave(true);
        dto.setConsiderInstrumentCalendar(false);

        String batchNo = scheduleService.generateSchedule(dto, 1L);
        List<ScheduleResultVO> results = scheduleService.getScheduleByBatchNo(batchNo);

        for (ScheduleResultVO r : results) {
            if (tomorrow.equals(r.getScheduleDate())) {
                assertNotEquals(user1.getId(), r.getDetectPersonId(),
                        "不应分配给休假用户 user1=" + user1.getId() + " on " + tomorrow);
            }
        }
        log.info("休假规避验证通过：排程结果中休假用户未被分配到请假日期");

        log.info("==================== 测试4通过 ====================");
    }

    @Test
    void testConflictDetection() {
        log.info("==================== 测试5：冲突检测 ====================");

        ScheduleResult sr1 = new ScheduleResult();
        sr1.setScheduleBatchNo("CONFLICT_TEST");
        sr1.setTaskId(taskHigh.getId());
        sr1.setTaskCode(taskHigh.getTaskCode());
        sr1.setSampleId(sample1.getId());
        sr1.setSampleCode(sample1.getSampleCode());
        sr1.setDetectItemId(detectItem1.getId());
        sr1.setDetectItemName(detectItem1.getItemName());
        sr1.setInstrumentId(instrument1.getId());
        sr1.setInstrumentName(instrument1.getInstrumentName());
        sr1.setDetectPersonId(user1.getId());
        sr1.setDetectPersonName(user1.getRealName());
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        while (tomorrow.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || tomorrow.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            tomorrow = tomorrow.plusDays(1);
        }
        sr1.setScheduleDate(tomorrow);
        sr1.setStartTime(tomorrow.atTime(9, 0));
        sr1.setEndTime(tomorrow.atTime(10, 0));
        sr1.setDurationMinutes(60);
        sr1.setPriority(TaskConstants.TASK_PRIORITY_HIGH);
        sr1.setStatus(ScheduleConstants.SCHEDULE_STATUS_SCHEDULED);
        sr1.setSource(ScheduleConstants.SCHEDULE_SOURCE_AUTO);
        sr1.setSortOrder(1);
        scheduleResultMapper.insert(sr1);

        ScheduleResult sr2 = new ScheduleResult();
        sr2.setScheduleBatchNo("CONFLICT_TEST");
        sr2.setTaskId(taskLow.getId());
        sr2.setTaskCode(taskLow.getTaskCode());
        sr2.setSampleId(sample2.getId());
        sr2.setSampleCode(sample2.getSampleCode());
        sr2.setDetectItemId(detectItem2.getId());
        sr2.setDetectItemName(detectItem2.getItemName());
        sr2.setInstrumentId(instrument1.getId());
        sr2.setInstrumentName(instrument1.getInstrumentName());
        sr2.setDetectPersonId(user1.getId());
        sr2.setDetectPersonName(user1.getRealName());
        sr2.setScheduleDate(tomorrow);
        sr2.setStartTime(tomorrow.atTime(9, 30));
        sr2.setEndTime(tomorrow.atTime(10, 30));
        sr2.setDurationMinutes(60);
        sr2.setPriority(TaskConstants.TASK_PRIORITY_LOW);
        sr2.setStatus(ScheduleConstants.SCHEDULE_STATUS_SCHEDULED);
        sr2.setSource(ScheduleConstants.SCHEDULE_SOURCE_AUTO);
        sr2.setSortOrder(2);
        scheduleResultMapper.insert(sr2);
        log.info("创建两条冲突排程：同一仪器+同一用户在09:00-10:30有重叠");

        List<ScheduleConflictVO> conflicts = scheduleService.checkConflicts(tomorrow, tomorrow.plusDays(1));
        log.info("冲突检测结果：发现 {} 个冲突", conflicts.size());

        assertTrue(conflicts.size() >= 1, "至少应检测到1个冲突");
        conflicts.forEach(c -> {
            log.info("冲突类型：{}，资源：{}，描述：{}，时间：{} ~ {}",
                    c.getConflictType(), c.getResourceName(), c.getConflictDescription(),
                    c.getConflictStart(), c.getConflictEnd());
        });

        boolean hasInstrumentConflict = conflicts.stream()
                .anyMatch(c -> "INSTRUMENT_CONFLICT".equals(c.getConflictType()));
        assertTrue(hasInstrumentConflict, "应检测到仪器时间冲突");

        log.info("==================== 测试5通过 ====================");
    }

    @Test
    void testDurationAccuracy() {
        log.info("==================== 测试6：排程时长准确性验证 ====================");

        int expectedMinutes = 30;
        ScheduleGenerateDTO dto = new ScheduleGenerateDTO();
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(5));
        dto.setAlgorithmType(ScheduleConstants.ALGORITHM_GREEDY);
        dto.setConsiderStaffLeave(false);
        dto.setConsiderInstrumentCalendar(false);

        String batchNo = scheduleService.generateSchedule(dto, 1L);
        List<ScheduleResultVO> results = scheduleService.getScheduleByBatchNo(batchNo);

        for (ScheduleResultVO r : results) {
            long actualMinutes = java.time.Duration.between(r.getStartTime(), r.getEndTime()).toMinutes();
            assertEquals(r.getDurationMinutes().intValue(), actualMinutes,
                    "记录的时长字段应等于实际时间差: " + r.getTaskCode());
            log.info("时长验证：{} 记录{}分钟 = 实际{}分钟 ✓", r.getTaskCode(), r.getDurationMinutes(), actualMinutes);
        }

        log.info("==================== 测试6通过 ====================");
    }
}
