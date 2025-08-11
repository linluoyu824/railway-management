package com.railway.management.common.section.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "创建区段请求体")
public class SectionCreateDto {
    @NotBlank(message = "区段名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "管理人ID不能为空")
    private Long managerId;

    private Long departmentId;

    @NotNull(message = "区段里程不能为空")
    private BigDecimal mileage;

    private String startPoint; // 起始点

    private String endPoint; // 终点
}