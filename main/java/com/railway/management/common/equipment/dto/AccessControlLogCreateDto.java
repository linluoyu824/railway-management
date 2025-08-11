package com.railway.management.common.equipment.dto;

import com.railway.management.common.equipment.model.AccessControlAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "记录门禁操作请求体")
public class AccessControlLogCreateDto {
    @NotNull
    private Long accessControlId;
    @NotNull
    private Long userId;
    @NotNull
    private AccessControlAction action;
    private boolean success = true;
    private String remark;
}