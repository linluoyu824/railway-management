package com.railway.management.equipment.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkInstructionUpdateDto {
    @JsonIgnore // ID将从URL路径中获取
    private Long id;

    @NotBlank(message = "名称不能为空")
    private String name;
}