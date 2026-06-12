package com.foodlab.detect.dto;

import lombok.Data;

import java.util.List;

@Data
public class DetectResultBatchSubmitDTO {

    private Long taskId;

    private Long userId;

    private List<DetectResultSubmitDTO> resultList;
}
