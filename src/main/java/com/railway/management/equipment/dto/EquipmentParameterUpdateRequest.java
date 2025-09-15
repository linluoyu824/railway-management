package com.railway.management.equipment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EquipmentParameterUpdateRequest {
    @NotNull
    private Long equipmentId;
    @NotEmpty
    @Valid
    private List<ParameterUpdateDto> parameters;
}