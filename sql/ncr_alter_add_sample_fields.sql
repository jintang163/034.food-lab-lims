-- =====================================================
-- NCR模块表结构变更脚本 - 补充样品关联字段
-- 适用场景：已有数据库需要新增字段时执行
-- =====================================================

USE food_lab_lims;

-- 1. 为 ncr_cause_analysis 添加样品关联字段
ALTER TABLE ncr_cause_analysis
    ADD COLUMN sample_id BIGINT NOT NULL COMMENT '关联样品ID（冗余，便于按样品查询）' AFTER ncr_code,
    ADD COLUMN sample_code VARCHAR(50) NOT NULL COMMENT '样品编号（冗余）' AFTER sample_id,
    ADD INDEX idx_sample_id (sample_id);

-- 2. 为 ncr_corrective_action 添加样品关联字段
ALTER TABLE ncr_corrective_action
    ADD COLUMN sample_id BIGINT NOT NULL COMMENT '关联样品ID（冗余，便于按样品查询）' AFTER ncr_code,
    ADD COLUMN sample_code VARCHAR(50) NOT NULL COMMENT '样品编号（冗余）' AFTER sample_id,
    ADD INDEX idx_sample_id (sample_id);

-- 3. 为 ncr_preventive_action 添加样品关联字段
ALTER TABLE ncr_preventive_action
    ADD COLUMN sample_id BIGINT NOT NULL COMMENT '关联样品ID（冗余，便于按样品查询）' AFTER ncr_code,
    ADD COLUMN sample_code VARCHAR(50) NOT NULL COMMENT '样品编号（冗余）' AFTER sample_id,
    ADD INDEX idx_sample_id (sample_id);

-- 4. 新增补偿表（如果不存在）
CREATE TABLE IF NOT EXISTS ncr_event_compensation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    event_type VARCHAR(50) NOT NULL DEFAULT 'detect_unqualified' COMMENT '事件类型 detect_unqualified检测不合格',
    biz_key VARCHAR(100) NOT NULL COMMENT '业务主键（如 detect_result_id）',
    detect_result_id BIGINT COMMENT '检测结果ID',
    sample_id BIGINT COMMENT '样品ID',
    sample_code VARCHAR(50) COMMENT '样品编号',
    detect_item_id BIGINT COMMENT '检测项目ID',
    detect_item_name VARCHAR(100) COMMENT '检测项目名称',
    task_id BIGINT COMMENT '任务ID',
    submitter_id BIGINT COMMENT '提交人ID',
    event_payload TEXT COMMENT '事件原始JSON数据',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '已重试次数',
    max_retry INT NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    compensation_status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态 pending待处理 processing处理中 success成功 failed失败',
    last_error_msg VARCHAR(1000) COMMENT '最后一次错误信息',
    last_retry_time DATETIME COMMENT '最后重试时间',
    next_retry_time DATETIME COMMENT '下次重试时间（指数退避）',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_event_biz (event_type, biz_key),
    INDEX idx_compensation_status (compensation_status),
    INDEX idx_next_retry_time (next_retry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NCR事件补偿记录表（失败重试）';
