package com.foodlab.audit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodlab.audit.dto.RetestAdoptDTO;
import com.foodlab.audit.dto.RetestResultSubmitDTO;
import com.foodlab.audit.dto.RetestSubmitDTO;
import com.foodlab.audit.entity.RetestRecord;
import com.foodlab.audit.vo.RetestCompareVO;
import com.foodlab.audit.vo.RetestRecordVO;

import java.util.List;

public interface RetestService extends IService<RetestRecord> {

    RetestRecordVO startRetest(RetestSubmitDTO dto, Long userId, String userName);

    RetestRecordVO submitRetestResult(RetestResultSubmitDTO dto, Long userId, String userName);

    RetestCompareVO compareRetestResult(Long retestId);

    RetestRecordVO adoptResult(RetestAdoptDTO dto, Long userId, String userName);

    List<RetestRecordVO> getRetestByTaskId(Long taskId);

    RetestRecordVO getRetestDetail(Long retestId);
}
