package com.railway.management.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipmentCreateNfcDto {
    @NotBlank
    private String nfcId;
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    @NotNull
    private Long departmentId;
}