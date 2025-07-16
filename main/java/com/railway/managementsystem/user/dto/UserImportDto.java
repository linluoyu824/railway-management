package com.railway.managementsystem.user.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * DTO for mapping rows from the user import Excel file.
 * The @ExcelProperty annotation maps the class field to the column header.
 */
@Data
public class UserImportDto {

    @ExcelProperty("工号")
    private String employeeId;

    @ExcelProperty("姓名")
    private String fullName;

    @ExcelProperty("所属段")
    private String section;

    @ExcelProperty("运用车间")
    private String workshop;

    @ExcelProperty("车队")
    private String team;

    @ExcelProperty("指导组")
    private String guidanceGroup;

    @ExcelProperty("拼音码")
    private String pinyinCode;

    @ExcelProperty("职务")
    private String jobTitle;

    @ExcelProperty("驾驶证别")
    private String driverLicenseType;

    @ExcelProperty("移动电话")
    private String mobilePhone;
}