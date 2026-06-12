package com.foodlab.report.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodlab.common.constant.TaskConstants;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.common.result.ResultCode;
import com.foodlab.common.utils.CodeGenerator;
import com.foodlab.detect.entity.DetectResult;
import com.foodlab.detect.mapper.DetectResultMapper;
import com.foodlab.report.dto.ReportGenerateDTO;
import com.foodlab.report.entity.DetectReport;
import com.foodlab.report.entity.ReportTemplate;
import com.foodlab.report.mapper.DetectReportMapper;
import com.foodlab.report.mapper.ReportTemplateMapper;
import com.foodlab.report.service.ReportService;
import com.foodlab.report.vo.ReportDetailVO;
import com.foodlab.sample.entity.Sample;
import com.foodlab.sample.mapper.SampleMapper;
import com.foodlab.task.entity.DetectTask;
import com.foodlab.task.mapper.DetectTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<DetectReportMapper, DetectReport> implements ReportService {

    private final DetectReportMapper detectReportMapper;
    private final ReportTemplateMapper reportTemplateMapper;
    private final SampleMapper sampleMapper;
    private final DetectTaskMapper detectTaskMapper;
    private final DetectResultMapper detectResultMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DetectReport generateReport(ReportGenerateDTO dto, Long userId) {
        Sample sample = sampleMapper.selectById(dto.getSampleId());
        if (sample == null) {
            throw new BusinessException(ResultCode.SAMPLE_NOT_FOUND);
        }

        DetectTask task = detectTaskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        if (!TaskConstants.TASK_STATUS_APPROVED.equals(task.getTaskStatus())) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR, "任务未审核通过，无法生成报告");
        }

        ReportTemplate template;
        if (dto.getTemplateId() != null) {
            template = reportTemplateMapper.selectById(dto.getTemplateId());
        } else {
            template = reportTemplateMapper.selectDefaultByType(
                    StrUtil.isNotBlank(dto.getReportType()) ? dto.getReportType() : "常规报告");
        }

        if (template == null) {
            throw new BusinessException(ResultCode.REPORT_GENERATE_FAIL, "未找到报告模板");
        }

        List<DetectResult> results = detectResultMapper.selectByTaskId(dto.getTaskId());
        List<ReportDetailVO.ReportResultItem> resultItems = buildResultItems(results);

        String reportContent = renderReportContent(template.getTemplateContent(), sample, results, resultItems);

        DetectReport report = new DetectReport();
        report.setReportCode(CodeGenerator.generateReportCode());
        report.setReportName(StrUtil.isNotBlank(dto.getReportName()) ? dto.getReportName() : sample.getSampleName() + "检测报告");
        report.setReportType(template.getTemplateType());
        report.setTemplateId(template.getId());
        report.setSampleId(sample.getId());
        report.setSampleCode(sample.getSampleCode());
        report.setTaskId(task.getId());
        report.setReportContent(reportContent);
        report.setReportStatus("draft");
        report.setDownloadCount(0);
        report.setCreateBy(userId);
        report.setUpdateBy(userId);

        save(report);

        return report;
    }

    @Override
    public ReportDetailVO getReportDetail(Long reportId) {
        DetectReport report = getById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.REPORT_NOT_FOUND);
        }
        ReportDetailVO vo = BeanUtil.copyProperties(report, ReportDetailVO.class);

        Sample sample = sampleMapper.selectById(report.getSampleId());
        if (sample != null) {
            vo.setSampleName(sample.getSampleName());
        }

        List<DetectResult> results = detectResultMapper.selectByTaskId(report.getTaskId());
        List<ReportDetailVO.ReportResultItem> resultItems = buildResultItems(results);
        vo.setResultItems(resultItems);

        return vo;
    }

    @Override
    public List<DetectReport> getReportsBySampleId(Long sampleId) {
        return detectReportMapper.selectBySampleId(sampleId);
    }

    @Override
    public IPage<DetectReport> getReportPage(int pageNum, int pageSize, String reportCode,
                                             String sampleCode, String reportStatus,
                                             String reportType, String startDate, String endDate) {
        Page<DetectReport> page = new Page<>(pageNum, pageSize);
        return detectReportMapper.selectReportPage(page, reportCode, sampleCode, reportStatus, reportType, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean issueReport(Long reportId, Long issuerId, String issuerName) {
        DetectReport report = getById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.REPORT_NOT_FOUND);
        }

        report.setReportStatus("issued");
        report.setIssueDate(LocalDate.now());
        report.setExpireDate(LocalDate.now().plusYears(1));
        report.setIssuerId(issuerId);
        report.setIssuerName(issuerName);
        report.setSealStatus("1");
        report.setUpdateBy(issuerId);

        return updateById(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean downloadReport(Long reportId) {
        DetectReport report = getById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.REPORT_NOT_FOUND);
        }
        report.setDownloadCount(report.getDownloadCount() + 1);
        return updateById(report);
    }

    @Override
    public void previewReport(Long reportId, jakarta.servlet.http.HttpServletResponse response) {
        DetectReport report = getById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.REPORT_NOT_FOUND);
        }

        try {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write(report.getReportContent());
        } catch (Exception e) {
            log.error("预览报告失败", e);
            throw new BusinessException(ResultCode.REPORT_GENERATE_FAIL, "预览报告失败");
        }
    }

    @Override
    public void exportReport(Long reportId, jakarta.servlet.http.HttpServletResponse response) {
        DetectReport report = getById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.REPORT_NOT_FOUND);
        }

        try {
            response.setContentType("application/pdf");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode(report.getReportName(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".pdf");

            downloadReport(reportId);
        } catch (Exception e) {
            log.error("导出报告失败", e);
            throw new BusinessException(ResultCode.REPORT_GENERATE_FAIL, "导出报告失败");
        }
    }

    private List<ReportDetailVO.ReportResultItem> buildResultItems(List<DetectResult> results) {
        return results.stream().map(r -> {
            ReportDetailVO.ReportResultItem item = new ReportDetailVO.ReportResultItem();
            item.setDetectItemName(r.getDetectItemName());
            item.setDetectMethod(r.getDetectMethod());
            item.setDetectStandard(r.getDetectStandard());

            if ("quantitative".equals(r.getResultType())) {
                item.setResultValue(r.getResultValue() != null ? r.getResultValue().toString() : "");
            } else {
                item.setResultValue(r.getQualitativeResult());
            }
            item.setResultUnit(r.getResultUnit());

            String limitValue = buildLimitValue(r);
            item.setLimitValue(limitValue);
            item.setFinalJudge(r.getFinalJudge());
            return item;
        }).collect(Collectors.toList());
    }

    private String buildLimitValue(DetectResult r) {
        if (r.getLimitType() == null) {
            return "";
        }
        switch (r.getLimitType()) {
            case "max":
                return "≤" + (r.getLimitValueMax() != null ? r.getLimitValueMax().toString() : "") +
                        (r.getResultUnit() != null ? " " + r.getResultUnit() : "");
            case "min":
                return "≥" + (r.getLimitValueMin() != null ? r.getLimitValueMin().toString() : "") +
                        (r.getResultUnit() != null ? " " + r.getResultUnit() : "");
            case "range":
                return (r.getLimitValueMin() != null ? r.getLimitValueMin().toString() : "") + "~" +
                        (r.getLimitValueMax() != null ? r.getLimitValueMax().toString() : "") +
                        (r.getResultUnit() != null ? " " + r.getResultUnit() : "");
            default:
                return "";
        }
    }

    private String renderReportContent(String templateContent, Sample sample,
                                        List<DetectResult> results,
                                        List<ReportDetailVO.ReportResultItem> resultItems) {
        if (StrUtil.isBlank(templateContent)) {
            return "";
        }

        String content = templateContent;
        content = content.replace("${reportCode}", CodeGenerator.generateReportCode());
        content = content.replace("${sampleName}", sample.getSampleName() != null ? sample.getSampleName() : "");
        content = content.replace("${sampleCode}", sample.getSampleCode() != null ? sample.getSampleCode() : "");
        content = content.replace("${batchNo}", sample.getBatchNo() != null ? sample.getBatchNo() : "");
        content = content.replace("${manufacturer}", sample.getManufacturer() != null ? sample.getManufacturer() : "");
        content = content.replace("${productionDate}", sample.getProductionDate() != null ? sample.getProductionDate() : "");
        content = content.replace("${sampleLocation}", sample.getSampleLocation() != null ? sample.getSampleLocation() : "");

        StringBuilder tableRows = new StringBuilder();
        for (ReportDetailVO.ReportResultItem item : resultItems) {
            tableRows.append("<tr>")
                    .append("<td>").append(item.getDetectItemName()).append("</td>")
                    .append("<td>").append(item.getResultValue()).append(" ")
                    .append(item.getResultUnit() != null ? item.getResultUnit() : "").append("</td>")
                    .append("<td>").append(item.getLimitValue()).append("</td>")
                    .append("<td>").append("qualified".equals(item.getFinalJudge()) ? "合格" : "不合格").append("</td>")
                    .append("</tr>");
        }

        content = content.replace("<#list results as item>", "");
        content = content.replace("</#list>", "");
        content = content.replace("${item.itemName}", "${item.itemName}");
        content = content.replace("${item.result}", "${item.result}");
        content = content.replace("${item.standard}", "${item.standard}");
        content = content.replace("${item.judge}", "${item.judge}");

        if (content.contains("<table") && content.contains("</table>")) {
            int tableStart = content.indexOf("<table");
            int tableEnd = content.indexOf("</table>") + 8;
            String tableTemplate = content.substring(tableStart, tableEnd);

            String resultsTable = "<table border=\"1\"><tr><th>检测项目</th><th>检测结果</th><th>标准要求</th><th>判定</th></tr>" +
                    tableRows + "</table>";

            content = content.substring(0, tableStart) + resultsTable + content.substring(tableEnd);
        }

        return content;
    }
}
