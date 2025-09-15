package com.railway.management.common.train.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.railway.management.common.train.model.TrainStatus;
import lombok.Data;

@Data
public class TrainExportDto {
    @ExcelProperty("列车号")
    private String trainNumber;

    @ExcelProperty("型号")
    private String model;

    @ExcelProperty("状态")
    private TrainStatus status;

    @ExcelProperty("所属部门")
    private String departmentName;
}