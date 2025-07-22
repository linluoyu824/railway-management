package com.railway.managementsystem.equipment.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EquipmentAssignDto {

    @NotEmpty(message = "设备ID列表不能为空")
    private List<Long> equipmentIds;

    @NotNull(message = "管理人员ID不能为空")
    private Long adminUserId;
}