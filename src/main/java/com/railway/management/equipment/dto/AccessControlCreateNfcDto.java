package com.railway.management.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccessControlCreateNfcDto {
    @NotBlank
    private String nfcId;
    @NotBlank
    private String name;
    private String location;
    @NotNull
    private Long departmentId;
}