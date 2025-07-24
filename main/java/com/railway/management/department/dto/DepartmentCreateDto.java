package com.railway.management.department.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentCreateDto {
    @NotBlank(message = "部门名称不能为空")
    private String name;

    private Long parentId; // 父部门ID，可以为null
}