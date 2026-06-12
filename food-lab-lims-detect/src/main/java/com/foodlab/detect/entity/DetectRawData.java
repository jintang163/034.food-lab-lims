package com.foodlab.detect.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("detect_raw_data")
public class DetectRawData {

    private Long id;

    private Long resultId;

    private Long sampleId;

    private Long detectItemId;

    private String dataKey;

    private String dataValue;

    private String dataType;

    private Integer sort;

    private String remark;

    private LocalDateTime createTime;
}
