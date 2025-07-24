package com.railway.management.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建权限请求体")
public class PermissionCreateDto {
    @NotBlank(message = "权限名称不能为空")
    private String name;

    @NotBlank(message = "权限代码不能为空")
    private String code;

    private String description;
}