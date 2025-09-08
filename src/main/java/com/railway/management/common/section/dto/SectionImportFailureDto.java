package com.railway.management.common.section.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 区段导入失败数据传输对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SectionImportFailureDto extends SectionImportDto {
    @ExcelProperty("失败原因")
    private String failureReason;
}