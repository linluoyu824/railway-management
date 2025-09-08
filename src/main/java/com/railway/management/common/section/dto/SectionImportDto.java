package com.railway.management.common.section.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 区段导入数据传输对象
 */
@Data
public class SectionImportDto {
    @ExcelProperty("区段名称")
    private String name;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("区段里程")
    private BigDecimal mileage;

    @ExcelProperty("起始点")
    private String startPoint;

    @ExcelProperty("终点")
    private String endPoint;

    @ExcelProperty("负责人ID")
    private Long managerId;

    @ExcelProperty("所属部门ID")
    private Long departmentId;
}