-- =====================================================
-- 食品检测LIMS系统数据库脚本
-- Database: MySQL 8.0+
-- =====================================================

CREATE DATABASE IF NOT EXISTS food_lab_lims DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE food_lab_lims;

-- =====================================================
-- 1. 系统管理模块
-- =====================================================

-- 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(50) NOT NULL COMMENT '部门编码',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    leader VARCHAR(20) COMMENT '负责人',
    phone VARCHAR(11) COMMENT '联系电话',
    email VARCHAR(50) COMMENT '邮箱',
    status CHAR(1) DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    dept_id BIGINT COMMENT '部门ID',
    username VARCHAR(30) NOT NULL UNIQUE COMMENT '用户账号',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(30) NOT NULL COMMENT '真实姓名',
    user_type VARCHAR(20) DEFAULT 'normal' COMMENT '用户类型（sampler采样员 inspector检测员 auditor审核员 admin管理员）',
    email VARCHAR(50) COMMENT '邮箱',
    phone VARCHAR(11) COMMENT '手机号码',
    gender CHAR(1) DEFAULT '0' COMMENT '性别（0男 1女 2未知）',
    avatar VARCHAR(100) COMMENT '头像地址',
    status CHAR(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
    login_ip VARCHAR(50) COMMENT '最后登录IP',
    login_time DATETIME COMMENT '最后登录时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(30) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(100) NOT NULL COMMENT '角色权限字符串',
    role_sort INT NOT NULL COMMENT '显示顺序',
    data_scope CHAR(1) DEFAULT '1' COMMENT '数据范围（1全部 2本部门 3本部门及以下 4仅本人）',
    status CHAR(1) DEFAULT '0' COMMENT '角色状态（0正常 1停用）',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) COMMENT '备注',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色信息表';

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和角色关联表';

-- 菜单表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    path VARCHAR(200) DEFAULT '' COMMENT '路由地址',
    component VARCHAR(255) COMMENT '组件路径',
    menu_type CHAR(1) DEFAULT 'M' COMMENT '菜单类型（M目录 C菜单 F按钮）',
    perms VARCHAR(100) COMMENT '权限标识',
    icon VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
    visible CHAR(1) DEFAULT '0' COMMENT '显示状态（0显示 1隐藏）',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';

-- =====================================================
-- 2. 检测项目管理模块
-- =====================================================

-- 检测项目类别表
DROP TABLE IF EXISTS detect_item_category;
CREATE TABLE detect_item_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '类别ID',
    category_name VARCHAR(50) NOT NULL COMMENT '类别名称',
    category_code VARCHAR(50) NOT NULL COMMENT '类别编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父类别ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测项目类别表';

-- 检测项目表
DROP TABLE IF EXISTS detect_item;
CREATE TABLE detect_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '检测项目ID',
    item_code VARCHAR(50) NOT NULL UNIQUE COMMENT '项目编码',
    item_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    category_id BIGINT NOT NULL COMMENT '所属类别ID',
    detect_method VARCHAR(200) COMMENT '检测方法',
    detect_standard VARCHAR(500) COMMENT '检测标准',
    unit VARCHAR(20) COMMENT '计量单位',
    precision_value DECIMAL(10,4) COMMENT '精度',
    form_schema JSON COMMENT '动态表单JSON Schema',
    status CHAR(1) DEFAULT '0' COMMENT '状态（0启用 1禁用）',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_item_code (item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测项目表';

-- 限量标准表
DROP TABLE IF EXISTS limit_standard;
CREATE TABLE limit_standard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标准ID',
    standard_name VARCHAR(200) NOT NULL COMMENT '标准名称',
    standard_no VARCHAR(50) NOT NULL COMMENT '标准编号',
    detect_item_id BIGINT NOT NULL COMMENT '检测项目ID',
    limit_type VARCHAR(20) NOT NULL COMMENT '限量类型（max最大 min最小 range区间 qualitative定性）',
    limit_value_min DECIMAL(12,4) COMMENT '最小值',
    limit_value_max DECIMAL(12,4) COMMENT '最大值',
    limit_unit VARCHAR(20) COMMENT '限量单位',
    qualitative_result VARCHAR(50) COMMENT '定性结果（合格/不合格）',
    description VARCHAR(500) COMMENT '描述',
    status CHAR(1) DEFAULT '0' COMMENT '状态（0启用 1禁用）',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='限量标准表';

-- =====================================================
-- 3. 样品管理模块
-- =====================================================

-- 样品表
DROP TABLE IF EXISTS sample_info;
CREATE TABLE sample_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '样品ID',
    sample_code VARCHAR(50) NOT NULL UNIQUE COMMENT '样品编号',
    sample_name VARCHAR(200) NOT NULL COMMENT '样品名称',
    batch_no VARCHAR(50) COMMENT '批号',
    manufacturer VARCHAR(200) COMMENT '生产单位',
    production_date VARCHAR(20) COMMENT '生产日期',
    shelf_life VARCHAR(50) COMMENT '保质期',
    sample_location VARCHAR(200) COMMENT '采样地点',
    sample_method VARCHAR(100) COMMENT '采样方式',
    sample_time DATETIME COMMENT '采样时间',
    sample_person VARCHAR(50) COMMENT '采样人',
    sample_amount VARCHAR(50) COMMENT '样品数量',
    sample_unit VARCHAR(20) COMMENT '样品单位',
    sample_status VARCHAR(20) DEFAULT 'pending' COMMENT '样品状态（pending待分配 detecting检测中 auditing审核中 completed已完成 rejected已驳回）',
    sync_status VARCHAR(20) DEFAULT 'success' COMMENT '同步状态（success成功 pending待同步 failed失败）',
    offline_id VARCHAR(64) COMMENT '离线ID',
    device_id VARCHAR(64) COMMENT '设备ID',
    detect_item_count INT DEFAULT 0 COMMENT '检测项目数',
    remark VARCHAR(1000) COMMENT '备注',
    attach_file VARCHAR(500) COMMENT '附件路径',
    bar_code VARCHAR(100) COMMENT '条形码内容',
    qr_code VARCHAR(500) COMMENT '二维码内容',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_sample_code (sample_code),
    KEY idx_sample_status (sample_status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='样品信息表';

-- 样品检测项目关联表
DROP TABLE IF EXISTS sample_detect_item;
CREATE TABLE sample_detect_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    sample_id BIGINT NOT NULL COMMENT '样品ID',
    sample_code VARCHAR(50) NOT NULL COMMENT '样品编号',
    detect_item_id BIGINT NOT NULL COMMENT '检测项目ID',
    detect_item_name VARCHAR(100) COMMENT '检测项目名称',
    limit_standard_id BIGINT COMMENT '限量标准ID',
    sort INT DEFAULT 0 COMMENT '排序',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_sample_id (sample_id),
    KEY idx_detect_item_id (detect_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='样品检测项目关联表';

-- =====================================================
-- 4. 任务管理模块
-- =====================================================

-- 检测任务表
DROP TABLE IF EXISTS detect_task;
CREATE TABLE detect_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_code VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编号',
    sample_id BIGINT NOT NULL COMMENT '样品ID',
    sample_code VARCHAR(50) NOT NULL COMMENT '样品编号',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(50) COMMENT '任务类型（常规委托 监督抽检 风险监测）',
    priority VARCHAR(20) DEFAULT 'normal' COMMENT '优先级（high高 normal中 low低）',
    task_status VARCHAR(20) DEFAULT 'pending' COMMENT '任务状态（pending待领取 in_progress检测中 completed检测完成 auditing审核中 approved已批准 rejected已驳回）',
    assign_by BIGINT COMMENT '分配人ID',
    assign_by_name VARCHAR(50) COMMENT '分配人姓名',
    assign_time DATETIME COMMENT '分配时间',
    detect_person_id BIGINT COMMENT '检测人员ID',
    detect_person_name VARCHAR(50) COMMENT '检测人员姓名',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    deadline DATETIME COMMENT '截止时间',
    remark VARCHAR(1000) COMMENT '备注',
    process_instance_id VARCHAR(64) COMMENT '工作流实例ID',
    detect_item_count INT DEFAULT 0 COMMENT '检测项目数',
    completed_item_count INT DEFAULT 0 COMMENT '已完成项目数',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_task_code (task_code),
    KEY idx_detect_person (detect_person_id),
    KEY idx_task_status (task_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测任务表';

-- 任务流转日志表
DROP TABLE IF EXISTS task_flow_log;
CREATE TABLE task_flow_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    task_code VARCHAR(50) NOT NULL COMMENT '任务编号',
    node_name VARCHAR(50) NOT NULL COMMENT '节点名称',
    operation VARCHAR(20) NOT NULL COMMENT '操作（assign分配 start开始 complete完成 submit提交 audit审核 reject驳回 approve批准）',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    opinion VARCHAR(1000) COMMENT '操作意见',
    previous_status VARCHAR(20) COMMENT '之前状态',
    current_status VARCHAR(20) COMMENT '当前状态',
    KEY idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务流转日志表';

-- =====================================================
-- 5. 检测结果模块
-- =====================================================

-- 检测结果主表
DROP TABLE IF EXISTS detect_result;
CREATE TABLE detect_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结果ID',
    result_code VARCHAR(50) NOT NULL UNIQUE COMMENT '结果编号',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    sample_id BIGINT NOT NULL COMMENT '样品ID',
    sample_code VARCHAR(50) NOT NULL COMMENT '样品编号',
    detect_item_id BIGINT NOT NULL COMMENT '检测项目ID',
    detect_item_name VARCHAR(100) NOT NULL COMMENT '检测项目名称',
    detect_method VARCHAR(200) COMMENT '检测方法',
    detect_standard VARCHAR(500) COMMENT '检测标准',
    instrument VARCHAR(100) COMMENT '检测仪器',
    detect_time DATETIME COMMENT '检测时间',
    detect_person_id BIGINT COMMENT '检测人ID',
    detect_person_name VARCHAR(50) COMMENT '检测人姓名',
    result_type VARCHAR(20) DEFAULT 'quantitative' COMMENT '结果类型（quantitative定量 qualitative定性）',
    result_value DECIMAL(18,4) COMMENT '定量结果值',
    result_unit VARCHAR(20) COMMENT '结果单位',
    qualitative_result VARCHAR(50) COMMENT '定性结果',
    limit_type VARCHAR(20) COMMENT '限量类型',
    limit_value_min DECIMAL(12,4) COMMENT '限量最小值',
    limit_value_max DECIMAL(12,4) COMMENT '限量最大值',
    auto_judge VARCHAR(20) COMMENT '自动判定结果（qualified合格 unqualified不合格 pending待判定）',
    manual_judge VARCHAR(20) COMMENT '人工判定结果',
    final_judge VARCHAR(20) COMMENT '最终判定结果',
    calculate_formula VARCHAR(500) COMMENT '计算公式',
    remark VARCHAR(1000) COMMENT '备注',
    attach_files JSON COMMENT '附件列表',
    is_audit CHAR(1) DEFAULT '0' COMMENT '是否审核（0否 1是）',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_result_code (result_code),
    KEY idx_task_id (task_id),
    KEY idx_sample_id (sample_id),
    KEY idx_detect_item_id (detect_item_id),
    KEY idx_final_judge (final_judge)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测结果主表';

-- 检测原始记录表
DROP TABLE IF EXISTS detect_raw_data;
CREATE TABLE detect_raw_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '原始数据ID',
    result_id BIGINT NOT NULL COMMENT '结果ID',
    sample_id BIGINT NOT NULL COMMENT '样品ID',
    detect_item_id BIGINT NOT NULL COMMENT '检测项目ID',
    data_key VARCHAR(100) NOT NULL COMMENT '数据键',
    data_value VARCHAR(500) NOT NULL COMMENT '数据值',
    data_type VARCHAR(20) DEFAULT 'string' COMMENT '数据类型',
    sort INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_result_id (result_id),
    KEY idx_sample_item (sample_id, detect_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测原始记录表';

-- =====================================================
-- 6. 审核管理模块
-- =====================================================

-- 审核记录表
DROP TABLE IF EXISTS audit_record;
CREATE TABLE audit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核ID',
    audit_code VARCHAR(50) NOT NULL UNIQUE COMMENT '审核编号',
    business_type VARCHAR(30) NOT NULL COMMENT '业务类型（sample样品 task任务 result结果 report报告）',
    business_id BIGINT NOT NULL COMMENT '业务ID',
    business_code VARCHAR(50) COMMENT '业务编号',
    audit_level INT DEFAULT 1 COMMENT '审核级别（1一审 2二审）',
    audit_status VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态（pending待审核 approved已通过 rejected已驳回）',
    auditor_id BIGINT COMMENT '审核人ID',
    auditor_name VARCHAR(50) COMMENT '审核人姓名',
    audit_time DATETIME COMMENT '审核时间',
    audit_opinion VARCHAR(2000) COMMENT '审核意见',
    previous_audit_id BIGINT COMMENT '上一级审核ID',
    next_audit_id BIGINT COMMENT '下一级审核ID',
    remark VARCHAR(1000) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_business (business_type, business_id),
    KEY idx_audit_status (audit_status),
    KEY idx_auditor (auditor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

-- =====================================================
-- 7. 报告管理模块
-- =====================================================

-- 报告模板表
DROP TABLE IF EXISTS report_template;
CREATE TABLE report_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    template_code VARCHAR(50) NOT NULL UNIQUE COMMENT '模板编号',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(30) NOT NULL COMMENT '模板类型（常规报告 监督报告 委托报告）',
    template_content TEXT COMMENT '模板内容（HTML或Freemarker格式）',
    template_file VARCHAR(500) COMMENT '模板文件路径',
    is_default CHAR(1) DEFAULT '0' COMMENT '是否默认（0否 1是）',
    status CHAR(1) DEFAULT '0' COMMENT '状态（0启用 1禁用）',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告模板表';

-- 检测报告表
DROP TABLE IF EXISTS detect_report;
CREATE TABLE detect_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    report_code VARCHAR(50) NOT NULL UNIQUE COMMENT '报告编号',
    report_name VARCHAR(200) NOT NULL COMMENT '报告名称',
    report_type VARCHAR(30) NOT NULL COMMENT '报告类型',
    template_id BIGINT COMMENT '模板ID',
    sample_id BIGINT NOT NULL COMMENT '样品ID',
    sample_code VARCHAR(50) NOT NULL COMMENT '样品编号',
    task_id BIGINT COMMENT '任务ID',
    report_content TEXT COMMENT '报告内容（HTML）',
    report_file_path VARCHAR(500) COMMENT '报告文件路径（PDF）',
    report_status VARCHAR(20) DEFAULT 'draft' COMMENT '报告状态（draft草稿 auditing审核中 approved已批准 issued已签发 archived已归档）',
    issue_date DATE COMMENT '签发日期',
    expire_date DATE COMMENT '报告有效期',
    issuer_id BIGINT COMMENT '签发人ID',
    issuer_name VARCHAR(50) COMMENT '签发人姓名',
    seal_status CHAR(1) DEFAULT '0' COMMENT '签章状态（0未签章 1已签章）',
    seal_file_path VARCHAR(500) COMMENT '签章文件路径',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_report_code (report_code),
    KEY idx_sample_id (sample_id),
    KEY idx_report_status (report_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测报告表';

-- =====================================================
-- 8. 消息通知模块
-- =====================================================

-- 消息通知表
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    notice_type VARCHAR(30) NOT NULL COMMENT '通知类型（task任务 audit审核 system系统）',
    notice_title VARCHAR(200) NOT NULL COMMENT '通知标题',
    notice_content TEXT COMMENT '通知内容',
    receiver_id BIGINT NOT NULL COMMENT '接收人ID',
    receiver_name VARCHAR(50) COMMENT '接收人姓名',
    business_type VARCHAR(30) COMMENT '关联业务类型',
    business_id BIGINT COMMENT '关联业务ID',
    is_read CHAR(1) DEFAULT '0' COMMENT '是否已读（0否 1是）',
    read_time DATETIME COMMENT '阅读时间',
    push_status CHAR(1) DEFAULT '0' COMMENT '推送状态（0未推送 1已推送 2推送失败）',
    push_time DATETIME COMMENT '推送时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- =====================================================
-- 9. 离线同步表
-- =====================================================

-- 离线同步记录表
DROP TABLE IF EXISTS offline_sync_record;
CREATE TABLE offline_sync_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '同步ID',
    sync_batch_no VARCHAR(64) NOT NULL UNIQUE COMMENT '同步批次号',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    sync_type VARCHAR(30) NOT NULL COMMENT '同步类型（upload上传 download下载）',
    data_type VARCHAR(30) NOT NULL COMMENT '数据类型（sample样品 result结果）',
    total_count INT DEFAULT 0 COMMENT '总数据量',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    fail_count INT DEFAULT 0 COMMENT '失败数量',
    sync_status VARCHAR(20) DEFAULT 'processing' COMMENT '同步状态（processing处理中 success成功 failed失败）',
    fail_details JSON COMMENT '失败详情',
    sync_start_time DATETIME COMMENT '同步开始时间',
    sync_end_time DATETIME COMMENT '同步结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_device_user (device_id, user_id),
    KEY idx_sync_status (sync_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='离线同步记录表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 初始化部门
INSERT INTO sys_dept (parent_id, dept_name, dept_code, order_num, leader, phone, email) VALUES
(0, '食品检测实验室', 'FOOD_LAB', 0, '张主任', '13800138001', 'director@foodlab.com'),
(1, '采样部', 'SAMPLING', 1, '李主管', '13800138002', 'sampling@foodlab.com'),
(1, '检测部', 'DETECT', 2, '王主管', '13800138003', 'detect@foodlab.com'),
(1, '审核部', 'AUDIT', 3, '赵主管', '13800138004', 'audit@foodlab.com'),
(1, '综合部', 'ADMIN', 4, '刘主管', '13800138005', 'admin@foodlab.com');

-- 初始化角色
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, remark) VALUES
('超级管理员', 'admin', 1, '1', '拥有全部权限'),
('采样员', 'sampler', 2, '4', '样品采集与登记'),
('检测员', 'inspector', 3, '4', '样品检测与结果录入'),
('审核员', 'auditor', 4, '2', '检测结果审核'),
('报告签发人', 'issuer', 5, '1', '报告审核与签发');

-- 初始化用户（密码：123456）
INSERT INTO sys_user (dept_id, username, password, real_name, user_type, email, phone, gender) VALUES
(5, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 'admin', 'admin@foodlab.com', '13800138000', '0'),
(2, 'sampler01', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '张采样', 'sampler', 'sampler01@foodlab.com', '13800138011', '0'),
(3, 'inspector01', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '李检测', 'inspector', 'inspector01@foodlab.com', '13800138021', '0'),
(3, 'inspector02', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '王检测', 'inspector', 'inspector02@foodlab.com', '13800138022', '1'),
(4, 'auditor01', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '赵一审', 'auditor', 'auditor01@foodlab.com', '13800138031', '0'),
(4, 'auditor02', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '钱二审', 'auditor', 'auditor02@foodlab.com', '13800138032', '1');

-- 初始化用户角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 3),
(5, 4),
(6, 4);

-- 初始化检测项目类别
INSERT INTO detect_item_category (category_name, category_code, parent_id, order_num) VALUES
('理化指标', 'PHYSICAL_CHEMICAL', 0, 1),
('微生物指标', 'MICROBIOLOGICAL', 0, 2),
('重金属指标', 'HEAVY_METAL', 0, 3),
('农药残留', 'PESTICIDE_RESIDUE', 0, 4),
('食品添加剂', 'FOOD_ADDITIVE', 0, 5);

-- 初始化检测项目
INSERT INTO detect_item (item_code, item_name, category_id, detect_method, detect_standard, unit, precision_value, form_schema, status) VALUES
('PH001', 'pH值', 1, 'pH计法', 'GB 5009.237-2016', '', 0.01, 
'{"type":"object","properties":{"phValue":{"type":"number","title":"pH值","placeholder":"请输入pH值","minimum":0,"maximum":14}},"required":["phValue"]}', '0'),
('PH002', '水分', 1, '直接干燥法', 'GB 5009.3-2016', 'g/100g', 0.001,
'{"type":"object","properties":{"sampleWeight":{"type":"number","title":"样品重量(g)"},"dishWeight":{"type":"number","title":"称量皿重量(g)"},"driedWeight":{"type":"number","title":"烘干后总重(g)"}},"required":["sampleWeight","dishWeight","driedWeight"]}', '0'),
('PH003', '灰分', 1, '灼烧法', 'GB 5009.4-2016', 'g/100g', 0.001,
'{"type":"object","properties":{"sampleWeight":{"type":"number","title":"样品重量(g)"},"crucibleWeight":{"type":"number","title":"坩埚重量(g)"},"ashWeight":{"type":"number","title":"灰分+坩埚重(g)"}},"required":["sampleWeight","crucibleWeight","ashWeight"]}', '0'),
('MB001', '菌落总数', 2, '平板计数法', 'GB 4789.2-2016', 'CFU/g', 0,
'{"type":"object","properties":{"dilution1":{"type":"number","title":"稀释度1菌落数"},"dilution2":{"type":"number","title":"稀释度2菌落数"},"dilutionFactor":{"type":"number","title":"稀释倍数"}},"required":["dilution1","dilution2","dilutionFactor"]}', '0'),
('MB002', '大肠菌群', 2, 'MPN法', 'GB 4789.3-2016', 'MPN/100g', 0,
'{"type":"object","properties":{"positiveTubes":{"type":"number","title":"阳性管数"},"dilution":{"type":"string","title":"稀释度","enum":["1:10","1:100","1:1000"]}},"required":["positiveTubes","dilution"]}', '0'),
('HM001', '铅', 3, '石墨炉原子吸收光谱法', 'GB 5009.12-2017', 'mg/kg', 0.001,
'{"type":"object","properties":{"concentration":{"type":"number","title":"浓度(mg/L)"},"volume":{"type":"number","title":"定容体积(mL)"},"weight":{"type":"number","title":"样品重量(g)"},"dilution":{"type":"number","title":"稀释倍数"}},"required":["concentration","volume","weight"]}', '0'),
('HM002', '镉', 3, '石墨炉原子吸收光谱法', 'GB 5009.15-2014', 'mg/kg', 0.001,
'{"type":"object","properties":{"concentration":{"type":"number","title":"浓度(mg/L)"},"volume":{"type":"number","title":"定容体积(mL)"},"weight":{"type":"number","title":"样品重量(g)"},"dilution":{"type":"number","title":"稀释倍数"}},"required":["concentration","volume","weight"]}', '0'),
('HM003', '砷', 3, '氢化物发生原子荧光法', 'GB 5009.11-2014', 'mg/kg', 0.001,
'{"type":"object","properties":{"concentration":{"type":"number","title":"浓度(mg/L)"},"volume":{"type":"number","title":"定容体积(mL)"},"weight":{"type":"number","title":"样品重量(g)"},"dilution":{"type":"number","title":"稀释倍数"}},"required":["concentration","volume","weight"]}', '0'),
('PR001', '敌敌畏', 4, '气相色谱法', 'GB 2763-2021', 'mg/kg', 0.001,
'{"type":"object","properties":{"peakArea":{"type":"number","title":"样品峰面积"},"standardPeakArea":{"type":"number","title":"标准品峰面积"},"standardConcentration":{"type":"number","title":"标准品浓度(mg/L)"}},"required":["peakArea","standardPeakArea","standardConcentration"]}', '0'),
('FA001', '苯甲酸', 5, '高效液相色谱法', 'GB 5009.28-2016', 'g/kg', 0.001,
'{"type":"object","properties":{"peakArea":{"type":"number","title":"样品峰面积"},"standardPeakArea":{"type":"number","title":"标准品峰面积"},"standardConcentration":{"type":"number","title":"标准品浓度(mg/L)"}},"required":["peakArea","standardPeakArea","standardConcentration"]}', '0'),
('FA002', '山梨酸', 5, '高效液相色谱法', 'GB 5009.28-2016', 'g/kg', 0.001,
'{"type":"object","properties":{"peakArea":{"type":"number","title":"样品峰面积"},"standardPeakArea":{"type":"number","title":"标准品峰面积"},"standardConcentration":{"type":"number","title":"标准品浓度(mg/L)"}},"required":["peakArea","standardPeakArea","standardConcentration"]}', '0');

-- 初始化限量标准
INSERT INTO limit_standard (standard_name, standard_no, detect_item_id, limit_type, limit_value_min, limit_value_max, limit_unit, qualitative_result, description, status) VALUES
('GB 2762-2022 食品中污染物限量', 'GB 2762-2022', 8, 'max', NULL, 0.2, 'mg/kg', NULL, '谷类制品铅限量', '0'),
('GB 2762-2022 食品中污染物限量', 'GB 2762-2022', 8, 'max', NULL, 0.3, 'mg/kg', NULL, '蔬菜铅限量', '0'),
('GB 2762-2022 食品中污染物限量', 'GB 2762-2022', 8, 'max', NULL, 0.5, 'mg/kg', NULL, '水果铅限量', '0'),
('GB 2762-2022 食品中污染物限量', 'GB 2762-2022', 9, 'max', NULL, 0.1, 'mg/kg', NULL, '谷物镉限量', '0'),
('GB 2762-2022 食品中污染物限量', 'GB 2762-2022', 9, 'max', NULL, 0.05, 'mg/kg', NULL, '新鲜蔬菜镉限量', '0'),
('GB 2762-2022 食品中污染物限量', 'GB 2762-2022', 10, 'max', NULL, 0.5, 'mg/kg', NULL, '谷物砷限量', '0'),
('GB 2762-2022 食品中污染物限量', 'GB 2762-2022', 10, 'max', NULL, 0.2, 'mg/kg', NULL, '蔬菜砷限量', '0'),
('GB 2763-2021 食品中农药最大残留限量', 'GB 2763-2021', 11, 'max', NULL, 0.02, 'mg/kg', NULL, '谷物敌敌畏限量', '0'),
('GB 2763-2021 食品中农药最大残留限量', 'GB 2763-2021', 11, 'max', NULL, 0.05, 'mg/kg', NULL, '蔬菜敌敌畏限量', '0'),
('GB 2760-2014 食品添加剂使用标准', 'GB 2760-2014', 12, 'max', NULL, 1.0, 'g/kg', NULL, '苯甲酸最大使用量', '0'),
('GB 2760-2014 食品添加剂使用标准', 'GB 2760-2014', 13, 'max', NULL, 1.0, 'g/kg', NULL, '山梨酸最大使用量', '0'),
('GB 2726-2016 熟肉制品卫生标准', 'GB 2726-2016', 4, 'max', NULL, 80000, 'CFU/g', NULL, '菌落总数限量', '0'),
('GB 2726-2016 熟肉制品卫生标准', 'GB 2726-2016', 5, 'max', NULL, 100, 'MPN/100g', NULL, '大肠菌群限量', '0'),
('GB 7101-2021 饮料卫生标准', 'GB 7101-2021', 1, 'range', 3.0, 9.0, '', NULL, '饮料pH值范围', '0'),
('GB 7101-2021 饮料卫生标准', 'GB 7101-2021', 2, 'max', NULL, 90.0, 'g/100g', NULL, '饮料水分限量', '0');

-- 初始化报告模板
INSERT INTO report_template (template_code, template_name, template_type, template_content, is_default, status) VALUES
('TPL001', '常规检测报告模板', '常规报告', '<!DOCTYPE html><html><head><title>食品检测报告</title></head><body><h1>食品检测报告</h1><p>报告编号：${reportCode}</p><p>样品名称：${sampleName}</p><p>样品编号：${sampleCode}</p><table border=\"1\"><tr><th>检测项目</th><th>检测结果</th><th>标准要求</th><th>判定</th></tr><#list results as item><tr><td>${item.itemName}</td><td>${item.result}</td><td>${item.standard}</td><td>${item.judge}</td></tr></#list></table></body></html>', '1', '0'),
('TPL002', '监督抽检报告模板', '监督报告', '<!DOCTYPE html><html><head><title>食品监督抽检报告</title></head><body><h1>食品监督抽检报告</h1><p>报告编号：${reportCode}</p><p>样品名称：${sampleName}</p><p>生产日期：${productionDate}</p><p>生产单位：${manufacturer}</p><table border=\"1\"><tr><th>检测项目</th><th>检测结果</th><th>标准要求</th><th>判定</th></tr><#list results as item><tr><td>${item.itemName}</td><td>${item.result}</td><td>${item.standard}</td><td>${item.judge}</td></tr></#list></table></body></html>', '0', '0'),
('TPL003', '委托检测报告模板', '委托报告', '<!DOCTYPE html><html><head><title>食品委托检测报告</title></head><body><h1>食品委托检测报告</h1><p>报告编号：${reportCode}</p><p>委托单位：${clientName}</p><p>样品名称：${sampleName}</p><table border=\"1\"><tr><th>检测项目</th><th>检测结果</th><th>标准要求</th><th>判定</th></tr><#list results as item><tr><td>${item.itemName}</td><td>${item.result}</td><td>${item.standard}</td><td>${item.judge}</td></tr></#list></table></body></html>', '0', '0');
