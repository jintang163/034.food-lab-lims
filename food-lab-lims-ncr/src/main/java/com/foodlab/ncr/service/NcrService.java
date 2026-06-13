package com.foodlab.ncr.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodlab.ncr.dto.*;
import com.foodlab.ncr.entity.NcrRecord;
import com.foodlab.ncr.vo.NcrDetailVO;

import java.util.List;

public interface NcrService extends IService<NcrRecord> {

    NcrRecord createNcr(NcrCreateDTO dto, Long userId);

    NcrDetailVO getNcrDetail(Long ncrId);

    IPage<NcrRecord> getNcrPage(NcrQueryDTO dto);

    List<NcrRecord> getNcrListBySampleId(Long sampleId);

    boolean submitRecheck(NcrRecheckSubmitDTO dto, Long userId);

    boolean submitCauseAnalysis(NcrCauseAnalysisSubmitDTO dto, Long userId);

    boolean submitCorrectiveAction(NcrActionSubmitDTO dto, Long userId);

    boolean submitPreventiveAction(NcrActionSubmitDTO dto, Long userId);

    boolean closeNcr(NcrCloseDTO dto, Long userId);

    boolean cancelNcr(Long ncrId, Long userId, String remark);

    NcrRecord autoCreateNcrFromDetectResult(Long detectResultId, Long userId);
}
