package com.foodlab.sample.dto;

import com.foodlab.common.domain.OfflineSyncData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SampleSyncDTO extends OfflineSyncData<SampleRegisterDTO> {

}
