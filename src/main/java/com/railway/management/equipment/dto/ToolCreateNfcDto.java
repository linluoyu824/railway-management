package com.railway.management.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ToolCreateNfcDto {
    @NotBlank
    private String nfcId;
    @NotBlank
    private String name;
    private String model;
}