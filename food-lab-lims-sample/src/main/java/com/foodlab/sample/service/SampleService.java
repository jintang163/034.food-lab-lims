package com.foodlab.sample.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodlab.common.domain.OfflineSyncResult;
import com.foodlab.sample.dto.SampleRegisterDTO;
import com.foodlab.sample.dto.SampleSyncDTO;
import com.foodlab.sample.entity.Sample;
import com.foodlab.sample.vo.SampleDetailVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SampleService extends IService<Sample> {

    Sample registerSample(SampleRegisterDTO dto, Long userId);

    SampleDetailVO getSampleDetail(Long sampleId);

    IPage<Sample> getSamplePage(int pageNum, int pageSize, String sampleName,
                                String sampleCode, String sampleStatus,
                                String startDate, String endDate);

    boolean updateSample(Long sampleId, SampleRegisterDTO dto);

    boolean deleteSample(Long sampleId);

    boolean batchDelete(List<Long> ids);

    OfflineSyncResult syncOfflineData(SampleSyncDTO syncDTO);

    String generateBarcode(Long sampleId);

    String generateQRCode(Long sampleId);

    void exportSample(List<Long> ids, HttpServletResponse response);

    void importSample(MultipartFile file, Long userId);

    boolean updateSampleStatus(Long sampleId, String status);
}
