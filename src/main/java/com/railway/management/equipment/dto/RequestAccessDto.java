package com.railway.management.equipment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestAccessDto {
    @NotNull(message = "工单ID不能为空")
    private Long workOrderId;

    @NotNull(message = "门禁ID不能为空")
    private Long accessControlId;
}