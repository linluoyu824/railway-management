package com.railway.management.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新权限请求体")
public class PermissionUpdateDto {
    @NotNull(message = "权限ID不能为空")
    private Long id;

    @NotBlank(message = "权限名称不能为空")
    private String name;

    @NotBlank(message = "权限代码不能为空")
    private String code;

    private String description;
}