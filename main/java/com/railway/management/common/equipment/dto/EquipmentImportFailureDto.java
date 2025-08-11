package com.railway.management.common.equipment.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EquipmentImportFailureDto extends EquipmentImportDto {

    @ExcelProperty(value = "失败原因", index = 7)
    private String failureReason;
}