package com.railway.management.equipment.dto;

import com.railway.management.equipment.model.AccessPermissionRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovePermissionRequestDto {

    @NotNull(message = "审批状态不能为空")
    private AccessPermissionRequestStatus status;

    private String remark; // 审批意见，尤其在拒绝时
}