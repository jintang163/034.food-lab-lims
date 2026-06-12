package com.foodlab.sample.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SampleExcelVO implements Serializable {

    @ExcelProperty("样品名称")
    private String sampleName;

    @ExcelProperty("样品编号")
    private String sampleCode;

    @ExcelProperty("批号")
    private String batchNo;

    @ExcelProperty("生产单位")
    private String manufacturer;

    @ExcelProperty("生产日期")
    private String productionDate;

    @ExcelProperty("保质期")
    private String shelfLife;

    @ExcelProperty("采样地点")
    private String sampleLocation;

    @ExcelProperty("采样方式")
    private String sampleMethod;

    @ExcelProperty("采样人")
    private String samplePerson;

    @ExcelProperty("样品数量")
    private String sampleAmount;

    @ExcelProperty("单位")
    private String sampleUnit;

    @ExcelProperty("检测项目")
    private String detectItems;

    @ExcelProperty("备注")
    private String remark;
}
