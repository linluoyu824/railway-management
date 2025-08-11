package com.railway.management.common.section.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新区段请求体")
public class SectionUpdateDto {

    @NotNull(message = "区段ID不能为空")
    private Long id;

    private String name;

    private String description;

    @NotNull(message = "管理人ID不能为空")
    private Long managerId;

     @NotNull(message = "区段里程不能为空")
    private BigDecimal mileage;

    private String startPoint; // 起始点

    private String endPoint; // 终点
}