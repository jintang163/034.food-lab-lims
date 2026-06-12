package com.foodlab.task.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TaskQueryDTO implements Serializable {

    private String taskCode;

    private String taskName;

    private String taskStatus;

    private String taskType;

    private String priority;

    private Long detectPersonId;

    private Long sampleId;

    private String sampleCode;

    private String startDate;

    private String endDate;
}
