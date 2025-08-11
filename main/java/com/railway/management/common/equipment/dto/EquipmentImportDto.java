package com.railway.management.common.equipment.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EquipmentImportDto {

    @ExcelProperty(value = "设备名称", index = 0)
    @ColumnWidth(20)
    private String name;

    @ExcelProperty(value = "设备型号", index = 1)
    @ColumnWidth(20)
    private String type;

    @ExcelProperty(value = "序列号", index = 2)
    @ColumnWidth(25)
    private String serialNumber;

    @ExcelProperty(value = "购置日期", index = 3)
    @DateTimeFormat("yyyy-MM-dd")
    @ColumnWidth(15)
    private LocalDate purchaseDate;

    @ExcelProperty(value = "设备状态", index = 4)
    @ColumnWidth(15)
    private String status;  // 状态将由导入逻辑转换为枚举

    @ExcelProperty(value = "管理人员ID", index = 5)
    @ColumnWidth(15)
    private Long adminUserId;

    @ExcelProperty(value = "参数 (JSON格式)", index = 6)
    @ColumnWidth(30)
    private String parametersJson; // 参数将由导入逻辑转换为 List<Parameter>

}