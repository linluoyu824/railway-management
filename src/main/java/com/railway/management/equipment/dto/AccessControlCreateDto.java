package com.railway.management.equipment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建门禁设备请求体")
public class AccessControlCreateDto {
    @NotBlank
    private String name;
    @NotBlank
    private String location;
    private String ipAddress;
    @NotNull
    private Long departmentId;
}