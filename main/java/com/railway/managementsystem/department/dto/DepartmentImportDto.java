package com.railway.managementsystem.department.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 用于映射部门导入Excel文件的数据传输对象。
 * 通过定义多列来表示部门的层级结构。
 */
@Data
public class DepartmentImportDto {

    @ExcelProperty("一级部门")
    private String levelOneDepartment;

    @ExcelProperty("二级部门")
    private String levelTwoDepartment;

    @ExcelProperty("三级部门")
    private String levelThreeDepartment;

    @ExcelProperty("四级部门")
    private String levelFourDepartment;
}