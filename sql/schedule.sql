-- ========================================
-- 智能任务排程模块数据库表
-- ========================================

-- 仪器设备表
CREATE TABLE IF NOT EXISTS `instrument` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `instrument_code` VARCHAR(50) NOT NULL COMMENT '仪器编号',
    `instrument_name` VARCHAR(100) NOT NULL COMMENT '仪器名称',
    `instrument_type` VARCHAR(50) NOT NULL COMMENT '仪器类型',
    `model` VARCHAR(100) COMMENT '型号规格',
    `manufacturer` VARCHAR(100) COMMENT '生产厂家',
    `purchase_date` DATE COMMENT '购置日期',
    `location` VARCHAR(100) COMMENT '存放位置',
    `status` VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态: AVAILABLE可用, MAINTENANCE维护中, BROKEN故障, CALIBRATING校准中',
    `daily_start_time` TIME NOT NULL DEFAULT '08:00:00' COMMENT '每日可用开始时间',
    `daily_end_time` TIME NOT NULL DEFAULT '18:00:00' COMMENT '每日可用结束时间',
    `maintenance_cycle_days` INT DEFAULT 30 COMMENT '维护周期(天)',
    `last_maintenance_date` DATE COMMENT '上次维护日期',
    `next_maintenance_date` DATE COMMENT '下次维护日期',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT,
    `update_by` BIGINT,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_instrument_code` (`instrument_code`),
    KEY `idx_instrument_type` (`instrument_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仪器设备表';

-- 检测项目-仪器关联表 (多对多，一个检测项目可能需要多台仪器)
CREATE TABLE IF NOT EXISTS `detect_item_instrument` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `detect_item_id` BIGINT NOT NULL COMMENT '检测项目ID',
    `instrument_id` BIGINT NOT NULL COMMENT '仪器ID',
    `estimated_duration_minutes` INT NOT NULL DEFAULT 30 COMMENT '预计检测时长(分钟)',
    `sort_order` INT NOT NULL DEFAULT 1 COMMENT '使用顺序(同一项目可能需要多台仪器按顺序使用)',
    `is_required` TINYINT NOT NULL DEFAULT 1 COMMENT '是否必须: 1是 0否',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT,
    `update_by` BIGINT,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_detect_item` (`detect_item_id`),
    KEY `idx_instrument` (`instrument_id`),
    UNIQUE KEY `uk_item_instrument` (`detect_item_id`, `instrument_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测项目-仪器关联表';

-- 仪器日历表 (记录仪器的占用、维护、空闲等)
CREATE TABLE IF NOT EXISTS `instrument_calendar` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `instrument_id` BIGINT NOT NULL COMMENT '仪器ID',
    `calendar_date` DATE NOT NULL COMMENT '日期',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME NOT NULL COMMENT '结束时间',
    `event_type` VARCHAR(20) NOT NULL COMMENT '事件类型: OCCUPIED占用, MAINTENANCE维护, RESERVED预约, UNAVAILABLE不可用',
    `task_id` BIGINT COMMENT '关联任务ID(占用时)',
    `title` VARCHAR(200) COMMENT '事件标题',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT,
    `update_by` BIGINT,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_instrument_date` (`instrument_id`, `calendar_date`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仪器日历表';

-- 人员休假表
CREATE TABLE IF NOT EXISTS `staff_leave` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '人员ID',
    `leave_type` VARCHAR(20) NOT NULL COMMENT '休假类型: ANNUAL年假, SICK病假, PERSONAL事假, MATERNITY产假, OTHER其他',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `start_time` TIME DEFAULT '00:00:00' COMMENT '开始时间',
    `end_time` TIME DEFAULT '23:59:59' COMMENT '结束时间',
    `half_day` VARCHAR(10) COMMENT '半天: AM上午, PM下午, NULL全天',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING待审批, APPROVED已批准, REJECTED已拒绝, CANCELLED已取消',
    `reason` VARCHAR(500) COMMENT '请假事由',
    `approver_id` BIGINT COMMENT '审批人ID',
    `approve_time` DATETIME COMMENT '审批时间',
    `approve_remark` VARCHAR(500) COMMENT '审批备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT,
    `update_by` BIGINT,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_date` (`user_id`, `start_date`, `end_date`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员休假表';

-- 排程结果表
CREATE TABLE IF NOT EXISTS `schedule_result` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `schedule_batch_no` VARCHAR(50) NOT NULL COMMENT '排程批次号',
    `task_id` BIGINT NOT NULL COMMENT '检测任务ID',
    `task_code` VARCHAR(50) COMMENT '任务编号',
    `sample_id` BIGINT COMMENT '样品ID',
    `sample_code` VARCHAR(50) COMMENT '样品编号',
    `detect_item_id` BIGINT COMMENT '检测项目ID',
    `detect_item_name` VARCHAR(100) COMMENT '检测项目名称',
    `instrument_id` BIGINT NOT NULL COMMENT '仪器ID',
    `instrument_name` VARCHAR(100) COMMENT '仪器名称',
    `detect_person_id` BIGINT COMMENT '检测人员ID',
    `detect_person_name` VARCHAR(50) COMMENT '检测人员姓名',
    `schedule_date` DATE NOT NULL COMMENT '排程日期',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `duration_minutes` INT NOT NULL COMMENT '预计时长(分钟)',
    `priority` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级: HIGH高, MEDIUM中, LOW低',
    `status` VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT '状态: SCHEDULED已排程, IN_PROGRESS进行中, COMPLETED已完成, CANCELLED已取消, ADJUSTED已调整',
    `source` VARCHAR(20) NOT NULL DEFAULT 'AUTO' COMMENT '来源: AUTO自动排程, MANUAL手动调整',
    `sort_order` INT NOT NULL DEFAULT 1 COMMENT '排序号',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT,
    `update_by` BIGINT,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_task_instrument` (`schedule_batch_no`, `task_id`, `detect_item_id`, `instrument_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_instrument_id` (`instrument_id`),
    KEY `idx_person_id` (`detect_person_id`),
    KEY `idx_schedule_date` (`schedule_date`),
    KEY `idx_status` (`status`),
    KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排程结果表';

-- 排程日志表
CREATE TABLE IF NOT EXISTS `schedule_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `schedule_batch_no` VARCHAR(50) NOT NULL COMMENT '排程批次号',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型: GENERATE生成, ADJUST调整, CANCEL取消, PUBLISH发布',
    `operation_content` TEXT COMMENT '操作内容(JSON格式)',
    `task_count` INT COMMENT '涉及任务数',
    `conflict_count` INT COMMENT '冲突数',
    `algorithm_type` VARCHAR(50) COMMENT '算法类型: GREEDY贪心, GENETIC遗传',
    `time_cost_ms` BIGINT COMMENT '耗时(毫秒)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by` BIGINT,
    PRIMARY KEY (`id`),
    KEY `idx_batch_no` (`schedule_batch_no`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排程日志表';

-- ========================================
-- 初始化数据
-- ========================================

-- 插入示例仪器
INSERT INTO `instrument` (`instrument_code`, `instrument_name`, `instrument_type`, `model`, `manufacturer`, `location`, `status`) VALUES
('INS001', '高效液相色谱仪', 'CHROMATOGRAPHY', 'Agilent 1260', '安捷伦', '理化实验室A区', 'AVAILABLE'),
('INS002', '气相色谱仪', 'CHROMATOGRAPHY', 'Thermo Trace 1310', '赛默飞', '理化实验室A区', 'AVAILABLE'),
('INS003', '原子吸收分光光度计', 'SPECTROSCOPY', 'PE AA900', '珀金埃尔默', '理化实验室B区', 'AVAILABLE'),
('INS004', '紫外可见分光光度计', 'SPECTROSCOPY', 'Shimadzu UV-2600', '岛津', '理化实验室B区', 'AVAILABLE'),
('INS005', '电子天平', 'BALANCE', 'Sartorius BSA224S', '赛多利斯', '前处理室', 'AVAILABLE'),
('INS006', 'pH计', 'PH_METER', 'Mettler FiveEasy', '梅特勒', '前处理室', 'AVAILABLE'),
('INS007', '微生物培养箱', 'INCUBATOR', 'Binder BD23', '宾德', '微生物实验室', 'AVAILABLE'),
('INS008', '超净工作台', 'CABINET', 'Airtech SW-CJ-1F', '安泰', '微生物实验室', 'AVAILABLE');

-- 插入检测项目-仪器关联 (假设检测项目ID已存在)
INSERT INTO `detect_item_instrument` (`detect_item_id`, `instrument_id`, `estimated_duration_minutes`, `sort_order`) VALUES
(1, 5, 10, 1),
(1, 4, 20, 2),
(2, 5, 10, 1),
(2, 1, 45, 2),
(3, 5, 10, 1),
(3, 3, 30, 2),
(4, 5, 10, 1),
(4, 2, 40, 2),
(5, 7, 60, 1),
(5, 8, 30, 2);

-- 插入人员休假示例
INSERT INTO `staff_leave` (`user_id`, `leave_type`, `start_date`, `end_date`, `status`, `reason`) VALUES
(2, 'ANNUAL', DATE_ADD(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'APPROVED', '年假休息'),
(3, 'SICK', DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'APPROVED', '身体不适');
