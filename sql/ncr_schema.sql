-- =====================================================
-- 不合格品处置流程(NCR)数据库脚本
-- Database: MySQL 8.0+
-- 描述：复检→原因分析→纠正措施→预防措施→关闭，全流程关联样品
-- =====================================================

USE food_lab_lims;

-- =====================================================
-- 1. 不合格品主记录表
-- =====================================================
DROP TABLE IF EXISTS ncr_record;
CREATE TABLE ncr_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ncr_code VARCHAR(50) NOT NULL UNIQUE COMMENT '不合格品编号 NCR+日期+8位',
    ncr_source VARCHAR(20) NOT NULL DEFAULT 'manual' COMMENT '来源 auto自动触发 manual手动创建',
    sample_id BIGINT NOT NULL COMMENT '关联样品ID',
    sample_code VARCHAR(50) NOT NULL COMMENT '样品编号（冗余，方便查询）',
    sample_name VARCHAR(200) COMMENT '样品名称（冗余，方便查询）',
    detect_result_id BIGINT NOT NULL COMMENT '关联检测结果ID',
    detect_item_id BIGINT NOT NULL COMMENT '关联检测项目ID',
    detect_item_name VARCHAR(100) NOT NULL COMMENT '检测项目名称（冗余）',
    unqualified_description VARCHAR(500) NOT NULL COMMENT '不合格情况描述',
    ncr_status VARCHAR(30) NOT NULL DEFAULT 'recheck' COMMENT '流程状态 recheck复检 cause_analysis原因分析 corrective_action纠正措施 preventive_action预防措施 closed已关闭 cancelled已取消',
    current_stage VARCHAR(30) NOT NULL DEFAULT 'recheck' COMMENT '当前阶段（与状态一致，便于前端展示）',
    recheck_time DATETIME COMMENT '复检完成时间',
    cause_analysis_time DATETIME COMMENT '原因分析完成时间',
    corrective_action_time DATETIME COMMENT '纠正措施完成时间',
    preventive_action_time DATETIME COMMENT '预防措施完成时间',
    close_time DATETIME COMMENT '关闭时间',
    close_person_id BIGINT COMMENT '关闭人ID',
    close_person_name VARCHAR(50) COMMENT '关闭人姓名',
    close_remark VARCHAR(500) COMMENT '关闭备注',
    remark VARCHAR(500) COMMENT '备注',
    attach_files TEXT COMMENT '附件JSON数组',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by BIGINT COMMENT '创建者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sample_id (sample_id),
    INDEX idx_sample_code (sample_code),
    INDEX idx_detect_result_id (detect_result_id),
    INDEX idx_ncr_status (ncr_status),
    INDEX idx_ncr_source (ncr_source),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不合格品处置主记录表';

-- =====================================================
-- 2. 复检记录表（可多次复检）
-- =====================================================
DROP TABLE IF EXISTS ncr_recheck;
CREATE TABLE ncr_recheck (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ncr_id BIGINT NOT NULL COMMENT '关联NCR主记录ID',
    ncr_code VARCHAR(50) NOT NULL COMMENT 'NCR编号（冗余）',
    recheck_count INT NOT NULL DEFAULT 1 COMMENT '复检次数（第几次复检）',
    recheck_status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '复检结果 pending待处理 qualified合格 unqualified不合格',
    task_id BIGINT COMMENT '关联复检任务ID',
    sample_id BIGINT NOT NULL COMMENT '关联样品ID（冗余，便于按样品查询）',
    sample_code VARCHAR(50) NOT NULL COMMENT '样品编号（冗余）',
    detect_item_id BIGINT NOT NULL COMMENT '关联检测项目ID',
    detect_item_name VARCHAR(100) NOT NULL COMMENT '检测项目名称（冗余）',
    detect_method VARCHAR(200) COMMENT '检测方法',
    detect_standard VARCHAR(200) COMMENT '检测标准',
    instrument VARCHAR(100) COMMENT '使用仪器',
    detect_time DATETIME COMMENT '复检检测时间',
    detect_person_id BIGINT COMMENT '复检检测人ID',
    detect_person_name VARCHAR(50) COMMENT '复检检测人姓名',
    result_type VARCHAR(20) COMMENT '结果类型 quantitative定量 qualitative定性',
    result_value DECIMAL(18,4) COMMENT '定量结果值',
    result_unit VARCHAR(20) COMMENT '结果单位',
    qualitative_result VARCHAR(100) COMMENT '定性结果',
    limit_type VARCHAR(20) COMMENT '限值类型 max min range qualitative',
    limit_value_min DECIMAL(18,4) COMMENT '最小限值',
    limit_value_max DECIMAL(18,4) COMMENT '最大限值',
    final_judge VARCHAR(20) NOT NULL COMMENT '最终判定 qualified合格 unqualified不合格 pending待判定',
    recheck_remark VARCHAR(500) COMMENT '复检备注说明',
    attach_files TEXT COMMENT '附件JSON数组',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by BIGINT COMMENT '创建者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_ncr_id (ncr_id),
    INDEX idx_sample_id (sample_id),
    INDEX idx_recheck_status (recheck_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不合格品复检记录表';

-- =====================================================
-- 3. 原因分析表
-- =====================================================
DROP TABLE IF EXISTS ncr_cause_analysis;
CREATE TABLE ncr_cause_analysis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ncr_id BIGINT NOT NULL COMMENT '关联NCR主记录ID',
    ncr_code VARCHAR(50) NOT NULL COMMENT 'NCR编号（冗余）',
    sample_id BIGINT NOT NULL COMMENT '关联样品ID（冗余，便于按样品查询）',
    sample_code VARCHAR(50) NOT NULL COMMENT '样品编号（冗余）',
    cause_type VARCHAR(30) NOT NULL COMMENT '原因类型 personnel人员 equipment设备 material物料 method方法 environment环境 other其他',
    cause_description VARCHAR(500) NOT NULL COMMENT '原因描述',
    root_cause VARCHAR(500) COMMENT '根本原因分析（5Why/鱼骨图结论）',
    impact_analysis VARCHAR(500) COMMENT '影响范围分析（其他批次/其他样品）',
    analysis_person_id BIGINT COMMENT '分析人ID',
    analysis_person_name VARCHAR(50) COMMENT '分析人姓名',
    analysis_time DATETIME COMMENT '分析时间',
    review_person_id VARCHAR(50) COMMENT '评审人ID',
    review_person_name VARCHAR(50) COMMENT '评审人姓名',
    review_time DATETIME COMMENT '评审时间',
    review_opinion VARCHAR(500) COMMENT '评审意见',
    remark VARCHAR(500) COMMENT '备注',
    attach_files TEXT COMMENT '附件JSON数组',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by BIGINT COMMENT '创建者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_ncr_id (ncr_id),
    INDEX idx_sample_id (sample_id),
    INDEX idx_cause_type (cause_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不合格品原因分析表';

-- =====================================================
-- 4. 纠正措施表（可多条，针对不同问题点）
-- =====================================================
DROP TABLE IF EXISTS ncr_corrective_action;
CREATE TABLE ncr_corrective_action (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ncr_id BIGINT NOT NULL COMMENT '关联NCR主记录ID',
    ncr_code VARCHAR(50) NOT NULL COMMENT 'NCR编号（冗余）',
    sample_id BIGINT NOT NULL COMMENT '关联样品ID（冗余，便于按样品查询）',
    sample_code VARCHAR(50) NOT NULL COMMENT '样品编号（冗余）',
    action_description VARCHAR(500) NOT NULL COMMENT '纠正措施描述（针对什么问题做什么）',
    action_plan TEXT COMMENT '详细实施计划步骤',
    action_person_id BIGINT COMMENT '责任人ID',
    action_person_name VARCHAR(50) COMMENT '责任人姓名',
    plan_start_time DATETIME COMMENT '计划开始时间',
    plan_end_time DATETIME COMMENT '计划完成时间',
    actual_start_time DATETIME COMMENT '实际开始时间',
    actual_end_time DATETIME COMMENT '实际完成时间',
    action_status VARCHAR(20) NOT NULL DEFAULT 'completed' COMMENT '措施状态 pending待开始 in_progress进行中 completed已完成 verified已验证',
    action_result VARCHAR(500) COMMENT '实施结果',
    effectiveness_evaluation VARCHAR(500) COMMENT '有效性评价（是否解决了问题）',
    verify_person_id BIGINT COMMENT '验证人ID',
    verify_person_name VARCHAR(50) COMMENT '验证人姓名',
    verify_time DATETIME COMMENT '验证时间',
    verify_opinion VARCHAR(500) COMMENT '验证意见',
    remark VARCHAR(500) COMMENT '备注',
    attach_files TEXT COMMENT '附件JSON数组',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by BIGINT COMMENT '创建者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_ncr_id (ncr_id),
    INDEX idx_sample_id (sample_id),
    INDEX idx_action_status (action_status),
    INDEX idx_action_person_id (action_person_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不合格品纠正措施表';

-- =====================================================
-- 5. 预防措施表（可多条，防止再发）
-- =====================================================
DROP TABLE IF EXISTS ncr_preventive_action;
CREATE TABLE ncr_preventive_action (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ncr_id BIGINT NOT NULL COMMENT '关联NCR主记录ID',
    ncr_code VARCHAR(50) NOT NULL COMMENT 'NCR编号（冗余）',
    sample_id BIGINT NOT NULL COMMENT '关联样品ID（冗余，便于按样品查询）',
    sample_code VARCHAR(50) NOT NULL COMMENT '样品编号（冗余）',
    action_description VARCHAR(500) NOT NULL COMMENT '预防措施描述（如何防止类似问题再发生）',
    action_plan TEXT COMMENT '详细实施计划',
    action_person_id BIGINT COMMENT '责任人ID',
    action_person_name VARCHAR(50) COMMENT '责任人姓名',
    plan_start_time DATETIME COMMENT '计划开始时间',
    plan_end_time DATETIME COMMENT '计划完成时间',
    actual_start_time DATETIME COMMENT '实际开始时间',
    actual_end_time DATETIME COMMENT '实际完成时间',
    action_status VARCHAR(20) NOT NULL DEFAULT 'completed' COMMENT '措施状态 pending待开始 in_progress进行中 completed已完成 verified已验证',
    action_result VARCHAR(500) COMMENT '实施结果',
    effectiveness_evaluation VARCHAR(500) COMMENT '有效性评价（是否能有效预防）',
    verify_person_id BIGINT COMMENT '验证人ID',
    verify_person_name VARCHAR(50) COMMENT '验证人姓名',
    verify_time DATETIME COMMENT '验证时间',
    verify_opinion VARCHAR(500) COMMENT '验证意见',
    remark VARCHAR(500) COMMENT '备注',
    attach_files TEXT COMMENT '附件JSON数组',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by BIGINT COMMENT '创建者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_ncr_id (ncr_id),
    INDEX idx_sample_id (sample_id),
    INDEX idx_action_status (action_status),
    INDEX idx_action_person_id (action_person_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不合格品预防措施表';

-- =====================================================
-- 6. NCR事件补偿记录表（用于自动启动失败重试）
-- =====================================================
DROP TABLE IF EXISTS ncr_event_compensation;
CREATE TABLE ncr_event_compensation (
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
