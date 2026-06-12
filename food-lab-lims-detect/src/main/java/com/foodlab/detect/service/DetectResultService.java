package com.foodlab.detect.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodlab.common.domain.OfflineSyncResult;
import com.foodlab.detect.dto.DetectResultSubmitDTO;
import com.foodlab.detect.dto.DetectResultSyncDTO;
import com.foodlab.detect.entity.DetectResult;
import com.foodlab.detect.vo.DetectResultDetailVO;

import java.util.List;

public interface DetectResultService extends IService<DetectResult> {

    DetectResult submitResult(DetectResultSubmitDTO dto, Long userId);

    DetectResultDetailVO getResultDetail(Long resultId);

    List<DetectResultDetailVO> getResultsByTaskId(Long taskId);

    List<DetectResultDetailVO> getResultsBySampleId(Long sampleId);

    IPage<DetectResult> getResultPage(int pageNum, int pageSize, String sampleCode,
                                      String detectItemName, String finalJudge,
                                      String startDate, String endDate);

    OfflineSyncResult syncOfflineData(DetectResultSyncDTO syncDTO);

    String autoJudge(Long detectItemId, DetectResultSubmitDTO dto);

    boolean batchSubmit(List<DetectResultSubmitDTO> dtoList, Long userId);
}
