-- =====================================================
-- 两级审核与复测机制 数据库脚本
-- Database: MySQL 8.0+
-- 描述：复测记录表、抽样复审表、审核表新字段
-- =====================================================

USE food_lab_lims;

-- =====================================================
-- 1. 复测记录表
-- =====================================================
DROP TABLE IF EXISTS retest_record;
CREATE TABLE retest_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    retest_code VARCHAR(50) NOT NULL UNIQUE COMMENT '复测编号 RT+日期+8位',
    original_result_id BIGINT NOT NULL COMMENT '原检测结果ID',
    task_id BIGINT NOT NULL COMMENT '关联任务ID',
    sample_code VARCHAR(50) COMMENT '样品编号（冗余）',
    detect_item_name VARCHAR(100) COMMENT '检测项目名称（冗余）',
    trigger_audit_id BIGINT NOT NULL COMMENT '触发的审核记录ID',
    trigger_reason VARCHAR(500) NOT NULL COMMENT '复测原因',
    original_value VARCHAR(100) COMMENT '原检测值',
    original_judge VARCHAR(20) COMMENT '原判定 qualified合格 unqualified不合格',
    retest_value VARCHAR(100) COMMENT '复测检测值',
    retest_judge VARCHAR(20) COMMENT '复测判定 qualified合格 unqualified不合格',
    adopted_result VARCHAR(20) COMMENT '采用结果 ORIGINAL采用原结果 RETEST采用复测结果',
    retest_status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT '复测状态 PENDING待检测 DETECTING检测中 COMPLETED已完成 ADOPTED已采用',
    retester_id BIGINT COMMENT '复测检测人ID',
    retester_name VARCHAR(50) COMMENT '复测检测人姓名',
    retest_time DATETIME COMMENT '复测完成时间',
    adopter_id BIGINT COMMENT '采用结果人ID',
    adopter_name VARCHAR(50) COMMENT '采用结果人姓名',
    adopt_time DATETIME COMMENT '采用时间',
    adopt_opinion VARCHAR(500) COMMENT '采用意见',
    process_instance_id VARCHAR(100) COMMENT '流程实例ID',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by BIGINT COMMENT '创建者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_id (task_id),
    INDEX idx_original_result_id (original_result_id),
    INDEX idx_trigger_audit_id (trigger_audit_id),
    INDEX idx_retest_status (retest_status),
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复测记录表';

-- =====================================================
-- 2. 抽样复审表
-- =====================================================
DROP TABLE IF EXISTS sampling_review;
CREATE TABLE sampling_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id BIGINT COMMENT '关联任务ID（每条任务一条记录）',
    task_code VARCHAR(50) COMMENT '任务编号（冗余）',
    sample_code VARCHAR(50) COMMENT '样品编号（冗余）',
    sample_rate DECIMAL(5,4) NOT NULL DEFAULT 0.10 COMMENT '抽样率 0.1=10%',
    review_type VARCHAR(20) NOT NULL DEFAULT 'RANDOM' COMMENT '复审类型 RANDOM随机抽样',
    review_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '复审状态 PENDING待复审 PASS已通过 REJECT已驳回',
    reviewer_id BIGINT COMMENT '复审人ID',
    reviewer_name VARCHAR(50) COMMENT '复审人姓名',
    review_time DATETIME COMMENT '复审时间',
    review_opinion VARCHAR(500) COMMENT '复审意见',
    task_ids_str TEXT COMMENT '抽取的任务ID列表（逗号分隔，用于关联批次）',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by BIGINT COMMENT '创建者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_id (task_id),
    INDEX idx_task_code (task_code),
    INDEX idx_sample_code (sample_code),
    INDEX idx_review_status (review_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽样复审表';

-- =====================================================
-- 3. 审核记录表新增字段
-- =====================================================
-- 如果表已存在，执行ALTER语句添加新字段
-- 使用存储过程判断字段是否存在，不存在则添加
DROP PROCEDURE IF EXISTS add_audit_record_columns;
DELIMITER //
CREATE PROCEDURE add_audit_record_columns()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'audit_record' 
                   AND COLUMN_NAME = 'retest_id') THEN
        ALTER TABLE audit_record ADD COLUMN retest_id BIGINT COMMENT '关联复测记录ID' AFTER next_audit_id;
    END IF;
    
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'audit_record' 
                   AND COLUMN_NAME = 'process_instance_id') THEN
        ALTER TABLE audit_record ADD COLUMN process_instance_id VARCHAR(100) COMMENT '流程实例ID' AFTER retest_id;
    END IF;
    
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'audit_record' 
                   AND COLUMN_NAME = 'action_type') THEN
        ALTER TABLE audit_record ADD COLUMN action_type VARCHAR(20) COMMENT '操作类型 APPROVE通过 REJECT驳回 RETEST复测' AFTER process_instance_id;
    END IF;
END //
DELIMITER ;
CALL add_audit_record_columns();
DROP PROCEDURE IF EXISTS add_audit_record_columns;

-- =====================================================
-- 4. Flowable 引擎表（如果未部署）
-- =====================================================
-- Flowable 6.x 引擎表请参考官方脚本：
-- https://github.com/flowable/flowable-engine/tree/master/modules/flowable-engine/src/main/resources/org/flowable/db/create

-- =====================================================
-- 5. 初始化示例数据
-- =====================================================
-- 复测示例数据
INSERT INTO retest_record (retest_code, original_result_id, task_id, sample_code, detect_item_name,
    trigger_audit_id, trigger_reason, original_value, original_judge,
    retest_value, retest_judge, retest_status, retester_id, retester_name, retest_time,
    process_instance_id, create_by, create_time)
VALUES
('RT20240115ABC12345', 1, 1, 'SP202401001', '蛋白质含量', 1, '检测结果接近限值，需复测确认',
 '0.05', 'qualified', NULL, NULL, 'PENDING', 2, '检测员B', NULL,
 'proc-retest-001', 1, NOW()),
('RT20240116DEF67890', 3, 2, 'SP202401002', '农药残留', 2, '检测数据异常',
 '2.5', 'unqualified', '1.2', 'qualified', 'COMPLETED', 3, '检测员C', '2024-01-16 15:30:00',
 'proc-retest-002', 1, NOW());

-- 抽样复审示例数据
INSERT INTO sampling_review (task_id, task_code, sample_code, sample_rate, review_type,
    review_status, reviewer_id, reviewer_name, review_time, review_opinion,
    task_ids_str, create_by, create_time)
VALUES
(1, 'TK202401001', 'SP202401001', 0.10, 'RANDOM', 'PENDING', NULL, NULL, NULL, NULL, '1,2,3', 4, NOW()),
(2, 'TK202401002', 'SP202401002', 0.10, 'RANDOM', 'PASS', 4, '质量管理员', '2024-01-16 15:30:00', '复核无误', '1,2,3', 4, NOW()),
(3, 'TK202401003', 'SP202401003', 0.10, 'RANDOM', 'REJECT', 4, '质量管理员', '2024-01-14 11:20:00', '检测数据有误，需要重新检测', '1,2,3', 4, NOW());
