package com.railway.management.equipment.dto;

import com.railway.management.equipment.model.AccessControlAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "门禁物理事件日志创建请求")
public class AccessControlLogCreateDto {

    @NotNull
    @Schema(description = "门禁设备ID")
    private Long accessControlId;

    @Schema(description = "用户ID (可选, 如果是设备自身事件则可能为空)")
    private Long userId;

    @Schema(description = "用户名 (可选)")
    private String username;

    @NotNull
    @Schema(description = "操作类型 (例如: DOOR_OPEN, DOOR_CLOSE, ALARM)")
    private AccessControlAction action;

    @Schema(description = "操作是否成功", defaultValue = "true")
    private boolean success = true;

    @Schema(description = "备注")
    private String remark;
}