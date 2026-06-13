package com.foodlab.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已禁用"),
    USER_EXIST(1004, "用户已存在"),
    TOKEN_INVALID(1005, "Token无效"),
    TOKEN_EXPIRED(1006, "Token已过期"),

    SAMPLE_NOT_FOUND(2001, "样品不存在"),
    SAMPLE_CODE_EXIST(2002, "样品编号已存在"),
    SAMPLE_STATUS_ERROR(2003, "样品状态错误"),

    TASK_NOT_FOUND(3001, "任务不存在"),
    TASK_STATUS_ERROR(3002, "任务状态错误"),
    TASK_ALREADY_ASSIGNED(3003, "任务已分配"),

    DETECT_ITEM_NOT_FOUND(4001, "检测项目不存在"),
    DETECT_RESULT_EXIST(4002, "检测结果已存在"),
    RESULT_NOT_FOUND(4003, "检测结果不存在"),

    AUDIT_NOT_FOUND(5001, "审核记录不存在"),
    AUDIT_STATUS_ERROR(5002, "审核状态错误"),

    REPORT_NOT_FOUND(6001, "报告不存在"),
    REPORT_GENERATE_FAIL(6002, "报告生成失败"),

    FILE_UPLOAD_FAIL(7001, "文件上传失败"),
    FILE_DOWNLOAD_FAIL(7002, "文件下载失败"),

    DATA_SYNC_FAIL(8001, "数据同步失败"),

    FORM_TEMPLATE_NOT_FOUND(9001, "表单模板不存在"),
    FORM_TEMPLATE_VERSION_NOT_FOUND(9002, "表单模板版本不存在"),
    FORM_DATA_NOT_FOUND(9003, "表单数据不存在"),
    FORM_SCHEMA_INVALID(9004, "表单Schema格式无效"),
    FORM_DATA_INVALID(9005, "表单数据验证失败"),
    FORM_TEMPLATE_STATUS_ERROR(9006, "表单模板状态错误"),
    FORM_VERSION_EXIST(9007, "表单版本已存在"),
    MIGRATION_NOT_FOUND(9008, "迁移记录不存在"),

    NCR_NOT_FOUND(10001, "不合格品记录不存在"),
    NCR_STATUS_ERROR(10002, "不合格品状态错误"),
    NCR_ALREADY_EXIST(10003, "不合格品记录已存在"),
    NCR_STAGE_ERROR(10004, "流程阶段错误");

    private final Integer code;
    private final String message;
}
