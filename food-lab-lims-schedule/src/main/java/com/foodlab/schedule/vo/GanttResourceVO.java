package com.foodlab.schedule.vo;

import lombok.Data;

@Data
public class GanttResourceVO {

    private Long id;
    private String name;
    private String type;
    private String code;
    private String status;
    private String location;
}
