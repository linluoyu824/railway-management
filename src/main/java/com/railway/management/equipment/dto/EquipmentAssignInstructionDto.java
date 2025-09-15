package com.railway.management.equipment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipmentAssignInstructionDto {
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    @NotNull(message = "作业指导书ID不能为空")
    private Long workInstructionId;
}