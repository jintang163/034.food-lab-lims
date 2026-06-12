package com.foodlab.detect.dto;

import lombok.Data;

@Data
public class DetectItemQueryDTO {

    private String itemName;

    private String itemCode;

    private Long categoryId;

    private String status;
}
