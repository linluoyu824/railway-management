package com.railway.management.common.user.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserImportFailureDto extends UserImportDto {

    @ExcelProperty(value = "失败原因", index = 10) // 确保索引在最后
    private String failureReason;
}