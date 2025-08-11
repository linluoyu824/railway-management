package com.railway.management.common.equipment.dto;

import com.railway.management.common.equipment.model.AccessControlStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新门禁设备请求体")
public class AccessControlUpdateDto {
    @NotNull
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String location;
    private String ipAddress;
    private AccessControlStatus status;
}