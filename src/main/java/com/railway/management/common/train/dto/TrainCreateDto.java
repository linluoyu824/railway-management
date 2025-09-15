package com.railway.management.common.train.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrainCreateDto {
    @NotBlank(message = "列车号不能为空")
    private String trainNumber;

    @NotBlank(message = "列车型号不能为空")
    private String model;

    @NotNull(message = "所属部门ID不能为空")
    private Long departmentId;
}