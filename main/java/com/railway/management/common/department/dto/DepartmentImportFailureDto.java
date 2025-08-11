package com.railway.management.common.department.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DepartmentImportFailureDto extends DepartmentImportDto {

    @ExcelProperty(value = "失败原因", index = 4)
    private String failureReason;
}