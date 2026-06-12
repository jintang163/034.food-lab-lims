package com.foodlab.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodlab.report.dto.ReportGenerateDTO;
import com.foodlab.report.entity.DetectReport;
import com.foodlab.report.vo.ReportDetailVO;

import java.util.List;

public interface ReportService extends IService<DetectReport> {

    DetectReport generateReport(ReportGenerateDTO dto, Long userId);

    ReportDetailVO getReportDetail(Long reportId);

    List<DetectReport> getReportsBySampleId(Long sampleId);

    IPage<DetectReport> getReportPage(int pageNum, int pageSize, String reportCode,
                                      String sampleCode, String reportStatus,
                                      String reportType, String startDate, String endDate);

    boolean issueReport(Long reportId, Long issuerId, String issuerName);

    boolean downloadReport(Long reportId);

    void previewReport(Long reportId, jakarta.servlet.http.HttpServletResponse response);

    void exportReport(Long reportId, jakarta.servlet.http.HttpServletResponse response);
}
