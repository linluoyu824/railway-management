package com.railway.management.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParameterUpdateDto {
    @NotNull
    private Long parameterId;
    @NotBlank
    private String value;
}