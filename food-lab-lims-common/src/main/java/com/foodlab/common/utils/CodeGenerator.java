package com.foodlab.common.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CodeGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generateSampleCode() {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        String uuid = IdUtil.objectId().toUpperCase();
        return "SP" + date + uuid.substring(0, 8).toUpperCase();
    }

    public static String generateTaskCode() {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        String uuid = IdUtil.objectId().toUpperCase();
        return "TK" + date + uuid.substring(0, 8).toUpperCase();
    }

    public static String generateReportCode() {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        String uuid = IdUtil.objectId().toUpperCase();
        return "RP" + date + uuid.substring(0, 8).toUpperCase();
    }

    public static String generateSyncBatchNo() {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        String uuid = IdUtil.objectId().toUpperCase();
        return "SYNC" + date + uuid.substring(0, 6).toUpperCase();
    }

    public static String generateResultCode() {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        String uuid = IdUtil.objectId().toUpperCase();
        return "RS" + date + uuid.substring(0, 8).toUpperCase();
    }

    public static String generateAuditCode() {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        String uuid = IdUtil.objectId().toUpperCase();
        return "AU" + date + uuid.substring(0, 8).toUpperCase();
    }

    public static String generateBarcode(String code) {
        return code;
    }

    public static String generateQRCodeContent(String code, String type) {
        return type + ":" + code;
    }
}
