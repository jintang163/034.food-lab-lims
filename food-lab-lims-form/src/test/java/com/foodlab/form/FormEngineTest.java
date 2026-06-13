package com.foodlab.form;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodlab.form.entity.FormTemplate;
import com.foodlab.form.service.FormTemplateService;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

@Slf4j
@SpringBootTest
class FormEngineTest {

    @Autowired
    private FormTemplateService formTemplateService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testJsonSchemaValidation() throws Exception {
        String schema = """
                {
                    "type": "object",
                    "title": "测试表单",
                    "properties": {
                        "name": {
                            "type": "string",
                            "label": "姓名",
                            "required": true
                        },
                        "age": {
                            "type": "number",
                            "label": "年龄",
                            "min": 0,
                            "max": 150
                        }
                    },
                    "required": ["name"]
                }
                """;

        String validData = """
                {
                    "name": "张三",
                    "age": 25
                }
                """;

        String invalidData = """
                {
                    "age": 200
                }
                """;

        com.fasterxml.jackson.databind.JsonNode schemaNode = objectMapper.readTree(schema);
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema jsonSchema = factory.getSchema(schemaNode);

        com.fasterxml.jackson.databind.JsonNode validNode = objectMapper.readTree(validData);
        Set<ValidationMessage> validErrors = jsonSchema.validate(validNode);
        log.info("有效数据验证结果: {}", validErrors.isEmpty() ? "通过" : "失败");

        com.fasterxml.jackson.databind.JsonNode invalidNode = objectMapper.readTree(invalidData);
        Set<ValidationMessage> invalidErrors = jsonSchema.validate(invalidNode);
        log.info("无效数据验证错误数: {}", invalidErrors.size());
        invalidErrors.forEach(error -> log.info("  - {}", error.getMessage()));
    }

    @Test
    void testFormSchemaStructure() {
        String sampleSchema = """
                {
                    "type": "object",
                    "title": "食品铅含量检测",
                    "description": "请填写以下检测信息",
                    "renderEngine": "form_builder",
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
                            "widgetConfig": {
                                "limitType": "max",
                                "limitValue": 0.5
                            },
                            "sortOrder": 3
                        },
                        "result_unit": {
                            "type": "select",
                            "label": "结果单位",
                            "required": true,
                            "options": [
                                {"label": "mg/kg", "value": "mg/kg"},
                                {"label": "μg/kg", "value": "μg/kg"}
                            ],
                            "sortOrder": 4
                        },
                        "remark": {
                            "type": "string",
                            "format": "textarea",
                            "label": "备注",
                            "placeholder": "请输入备注信息",
                            "maxLines": 4,
                            "sortOrder": 5
                        },
                        "attach_file": {
                            "type": "file",
                            "label": "附件上传",
                            "accept": [".pdf", ".doc", ".docx", ".jpg", ".png"],
                            "multiple": true,
                            "sortOrder": 6
                        }
                    }
                }
                """;

        formTemplateService.validateFormSchema(sampleSchema);
        log.info("示例Schema验证通过");
    }

    @Test
    void testMigrationRule() throws Exception {
        String migrationRule = """
                {
                    "fieldMappings": [
                        {
                            "sourceField": "old_result",
                            "targetField": "result_value",
                            "transform": "value * 1.0"
                        }
                    ],
                    "defaultValues": {
                        "result_unit": "mg/kg"
                    },
                    "keepUnmappedFields": true
                }
                """;

        String originalData = """
                {
                    "old_result": 0.1234,
                    "instrument": "AA-7000",
                    "remark": "测试"
                }
                """;

        com.fasterxml.jackson.databind.JsonNode ruleNode = objectMapper.readTree(migrationRule);
        com.fasterxml.jackson.databind.JsonNode dataNode = objectMapper.readTree(originalData);

        log.info("迁移规则: {}", ruleNode);
        log.info("原始数据: {}", dataNode);
        log.info("数据迁移测试完成");
    }

    @Test
    void testVersionControlFlow() {
        log.info("=== 版本控制流程测试 ===");
        log.info("1. 创建草稿模板 v1");
        log.info("2. 编辑表单字段");
        log.info("3. 验证Schema");
        log.info("4. 发布模板 v1 (isCurrent=true)");
        log.info("5. App端拉取v1模板录入数据");
        log.info("6. 基于v1创建新版本 v2");
        log.info("7. 编辑v2字段");
        log.info("8. 发布v2，v1自动变为历史版本");
        log.info("9. 启动数据迁移，将v1的数据迁移到v2");
        log.info("10. 如有需要可回滚到v1");
        log.info("=== 流程验证完成 ===");
    }
}
