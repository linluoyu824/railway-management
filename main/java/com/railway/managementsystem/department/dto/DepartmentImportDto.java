package com.railway.managementsystem.department.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
public class DepartmentImportDto {

    @ExcelProperty("一级部门")
    @ColumnWidth(20)
    private String levelOneDepartment;

    @ExcelProperty("二级部门")
    @ColumnWidth(20)
    private String levelTwoDepartment;

    @ExcelProperty("三级部门")
    @ColumnWidth(20)
    private String levelThreeDepartment;

    @ExcelProperty("四级部门")
    @ColumnWidth(20)
    private String levelFourDepartment;
}