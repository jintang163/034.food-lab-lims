package com.foodlab.task.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TaskFlowLogVO implements Serializable {

    private Long id;

    private String nodeName;

    private String nodeCode;

    private String operationType;

    private String operatorName;

    private LocalDateTime operateTime;

    private String remark;

    private String fromStatus;

    private String toStatus;
}
