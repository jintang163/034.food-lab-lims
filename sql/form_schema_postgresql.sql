-- =====================================================
-- 动态表单引擎 - PostgreSQL数据库脚本
-- Database: PostgreSQL 14+
-- 功能：表单模板管理、版本控制、动态数据存储
-- =====================================================

CREATE DATABASE food_lab_lims_form WITH ENCODING 'UTF8';

\c food_lab_lims_form;

-- =====================================================
-- 1. 表单模板表 (form_template)
-- 存储动态表单的JSON Schema定义
-- =====================================================
DROP TABLE IF EXISTS form_template CASCADE;
CREATE TABLE form_template (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(50) NOT NULL UNIQUE,
    template_name VARCHAR(100) NOT NULL,
    detect_item_id BIGINT NOT NULL,
    detect_item_name VARCHAR(100),
    description VARCHAR(500),
    form_schema JSONB NOT NULL,
    version INT NOT NULL DEFAULT 1,
    is_current BOOLEAN NOT NULL DEFAULT true,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    publish_time TIMESTAMP,
    published_by BIGINT,
    remark VARCHAR(500),
    deleted SMALLINT NOT NULL DEFAULT 0,
    create_by BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_template_code ON form_template(template_code);
CREATE INDEX idx_detect_item_id ON form_template(detect_item_id);
CREATE INDEX idx_is_current ON form_template(is_current);
CREATE INDEX idx_form_schema_gin ON form_template USING GIN (form_schema);

COMMENT ON TABLE form_template IS '表单模板表';
COMMENT ON COLUMN form_template.template_code IS '模板编码';
COMMENT ON COLUMN form_template.template_name IS '模板名称';
COMMENT ON COLUMN form_template.detect_item_id IS '关联检测项目ID';
COMMENT ON COLUMN form_template.detect_item_name IS '检测项目名称';
COMMENT ON COLUMN form_template.description IS '模板描述';
COMMENT ON COLUMN form_template.form_schema IS '表单JSON Schema (JSONB)';
COMMENT ON COLUMN form_template.version IS '版本号';
COMMENT ON COLUMN form_template.is_current IS '是否当前版本';
COMMENT ON COLUMN form_template.status IS '状态（draft草稿 published已发布 archived已归档）';
COMMENT ON COLUMN form_template.publish_time IS '发布时间';
COMMENT ON COLUMN form_template.published_by IS '发布人';

-- =====================================================
-- 2. 表单模板版本历史表 (form_template_version)
-- 存储模板的历史版本，用于版本回溯
-- =====================================================
DROP TABLE IF EXISTS form_template_version CASCADE;
CREATE TABLE form_template_version (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    detect_item_id BIGINT,
    form_schema JSONB NOT NULL,
    version INT NOT NULL,
    change_type VARCHAR(20) NOT NULL,
    change_summary VARCHAR(500),
    change_detail TEXT,
    publish_time TIMESTAMP,
    published_by BIGINT,
    previous_version_id BIGINT,
    next_version_id BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0,
    create_by BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_template_id ON form_template_version(template_id);
CREATE INDEX idx_template_code_version ON form_template_version(template_code, version);
CREATE INDEX idx_form_schema_version_gin ON form_template_version USING GIN (form_schema);

COMMENT ON TABLE form_template_version IS '表单模板版本历史表';
COMMENT ON COLUMN form_template_version.template_id IS '关联模板ID';
COMMENT ON COLUMN form_template_version.change_type IS '变更类型（create创建 update更新 migrate迁移）';
COMMENT ON COLUMN form_template_version.change_summary IS '变更摘要';
COMMENT ON COLUMN form_template_version.previous_version_id IS '上一版本ID';
COMMENT ON COLUMN form_template_version.next_version_id IS '下一版本ID';

-- =====================================================
-- 3. 表单数据表 (form_data)
-- 存储用户提交的动态表单数据 (JSONB)
-- =====================================================
DROP TABLE IF EXISTS form_data CASCADE;
CREATE TABLE form_data (
    id BIGSERIAL PRIMARY KEY,
    data_code VARCHAR(64) NOT NULL UNIQUE,
    template_id BIGINT NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    template_version INT NOT NULL,
    detect_item_id BIGINT,
    sample_id BIGINT,
    sample_code VARCHAR(50),
    task_id BIGINT,
    form_data JSONB NOT NULL,
    submit_time TIMESTAMP,
    submitted_by BIGINT,
    submitted_by_name VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    sync_status VARCHAR(20) DEFAULT 'success',
    offline_id VARCHAR(64),
    device_id VARCHAR(64),
    remark VARCHAR(500),
    deleted SMALLINT NOT NULL DEFAULT 0,
    create_by BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_template_id_version ON form_data(template_id, template_version);
CREATE INDEX idx_sample_id ON form_data(sample_id);
CREATE INDEX idx_task_id ON form_data(task_id);
CREATE INDEX idx_detect_item_id_data ON form_data(detect_item_id);
CREATE INDEX idx_submitted_by ON form_data(submitted_by);
CREATE INDEX idx_form_data_gin ON form_data USING GIN (form_data);

COMMENT ON TABLE form_data IS '表单数据表';
COMMENT ON COLUMN form_data.data_code IS '数据编码';
COMMENT ON COLUMN form_data.template_id IS '关联模板ID';
COMMENT ON COLUMN form_data.template_code IS '模板编码';
COMMENT ON COLUMN form_data.template_version IS '使用的模板版本';
COMMENT ON COLUMN form_data.detect_item_id IS '检测项目ID';
COMMENT ON COLUMN form_data.sample_id IS '关联样品ID';
COMMENT ON COLUMN form_data.sample_code IS '样品编码';
COMMENT ON COLUMN form_data.task_id IS '关联任务ID';
COMMENT ON COLUMN form_data.form_data IS '表单数据 (JSONB)';
COMMENT ON COLUMN form_data.status IS '状态（draft草稿 submitted已提交 audited已审核）';
COMMENT ON COLUMN form_data.sync_status IS '同步状态（success成功 pending待同步 failed失败）';
COMMENT ON COLUMN form_data.offline_id IS '离线数据ID';
COMMENT ON COLUMN form_data.device_id IS '设备ID';

-- =====================================================
-- 4. 数据迁移记录表 (data_migration_log)
-- 记录旧版本数据向新版本模板的迁移历史
-- =====================================================
DROP TABLE IF EXISTS data_migration_log CASCADE;
CREATE TABLE data_migration_log (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    from_version INT NOT NULL,
    to_version INT NOT NULL,
    migration_rule JSONB,
    total_count INT NOT NULL DEFAULT 0,
    success_count INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    failed_data_ids JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    error_message TEXT,
    deleted SMALLINT NOT NULL DEFAULT 0,
    create_by BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_migration_template_version ON data_migration_log(template_id, from_version, to_version);

COMMENT ON TABLE data_migration_log IS '数据迁移记录表';
COMMENT ON COLUMN data_migration_log.from_version IS '源版本号';
COMMENT ON COLUMN data_migration_log.to_version IS '目标版本号';
COMMENT ON COLUMN data_migration_log.migration_rule IS '迁移规则 (JSONB)';
COMMENT ON COLUMN data_migration_log.total_count IS '总迁移条数';
COMMENT ON COLUMN data_migration_log.success_count IS '成功条数';
COMMENT ON COLUMN data_migration_log.failed_count IS '失败条数';
COMMENT ON COLUMN data_migration_log.failed_data_ids IS '失败数据ID列表';

-- =====================================================
-- 5. 表单字段映射表 (form_field_mapping)
-- 存储模板字段与业务字段的映射关系
-- =====================================================
DROP TABLE IF EXISTS form_field_mapping CASCADE;
CREATE TABLE form_field_mapping (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    field_key VARCHAR(100) NOT NULL,
    field_label VARCHAR(100) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    business_table VARCHAR(50),
    business_column VARCHAR(50),
    is_result_field BOOLEAN NOT NULL DEFAULT false,
    is_required BOOLEAN NOT NULL DEFAULT false,
    sort_order INT NOT NULL DEFAULT 0,
    deleted SMALLINT NOT NULL DEFAULT 0,
    create_by BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_template_field ON form_field_mapping(template_id, field_key);
CREATE INDEX idx_result_field ON form_field_mapping(is_result_field);

COMMENT ON TABLE form_field_mapping IS '表单字段映射表';
COMMENT ON COLUMN form_field_mapping.field_key IS '字段Key';
COMMENT ON COLUMN form_field_mapping.field_label IS '字段标签';
COMMENT ON COLUMN form_field_mapping.field_type IS '字段类型（text number date select file等）';
COMMENT ON COLUMN form_field_mapping.business_table IS '关联业务表';
COMMENT ON COLUMN form_field_mapping.business_column IS '关联业务列';
COMMENT ON COLUMN form_field_mapping.is_result_field IS '是否为结果字段';

-- =====================================================
-- 插入示例数据
-- =====================================================

-- 插入示例表单模板
INSERT INTO form_template (
    template_code, template_name, detect_item_id, detect_item_name, 
    description, form_schema, version, is_current, status
) VALUES (
    'TPL-001', '食品铅含量检测录入表单', 1, '铅(Pb)',
    '用于检测食品中铅含量的录入表单，包含仪器信息、检测结果、备注等字段',
    '{
        "type": "object",
        "title": "食品铅含量检测",
        "description": "请填写以下检测信息",
        "properties": {
            "instrument": {
                "type": "string",
                "label": "检测仪器",
                "placeholder": "请输入仪器名称",
                "required": true,
                "sortOrder": 1
            },
            "detect_time": {
                "type": "string",
                "format": "date",
                "label": "检测日期",
                "placeholder": "请选择检测日期",
                "required": true,
                "sortOrder": 2
            },
            "result_value": {
                "type": "number",
                "label": "检测结果",
                "placeholder": "请输入检测结果",
                "unit": "mg/kg",
                "required": true,
                "min": 0,
                "precision": 4,
                "isResultField": true,
                "sortOrder": 3
            },
            "result_unit": {
                "type": "select",
                "label": "结果单位",
                "required": true,
                "options": [
                    {"label": "mg/kg", "value": "mg/kg"},
                    {"label": "μg/kg", "value": "μg/kg"},
                    {"label": "ppm", "value": "ppm"}
                ],
                "sortOrder": 4
            },
            "temperature": {
                "type": "number",
                "label": "环境温度",
                "placeholder": "请输入环境温度",
                "unit": "℃",
                "required": false,
                "sortOrder": 5
            },
            "humidity": {
                "type": "number",
                "label": "环境湿度",
                "placeholder": "请输入环境湿度",
                "unit": "%",
                "required": false,
                "min": 0,
                "max": 100,
                "sortOrder": 6
            },
            "remark": {
                "type": "string",
                "format": "textarea",
                "label": "备注",
                "placeholder": "请输入备注信息",
                "required": false,
                "maxLines": 4,
                "sortOrder": 7
            },
            "attach_file": {
                "type": "file",
                "label": "附件上传",
                "placeholder": "点击上传",
                "required": false,
                "accept": [".pdf", ".doc", ".docx", ".jpg", ".png"],
                "multiple": true,
                "sortOrder": 8
            }
        }
    }',
    1, true, 'published'
);

-- 插入示例表单数据
INSERT INTO form_data (
    data_code, template_id, template_code, template_version,
    detect_item_id, sample_id, sample_code, task_id,
    form_data, status, submitted_by, submitted_by_name
) VALUES (
    'DATA-001', 1, 'TPL-001', 1,
    1, 1, 'YP202401001', 1,
    '{
        "instrument": "原子吸收光谱仪 AA-7000",
        "detect_time": "2024-01-15",
        "result_value": 0.1234,
        "result_unit": "mg/kg",
        "temperature": 25.5,
        "humidity": 60,
        "remark": "检测过程正常，结果符合标准要求",
        "attach_file": ["uploads/2024/01/report_aa7000.pdf", "uploads/2024/01/spectrum.jpg"]
    }',
    'submitted', 1, '张三'
);
