package com.railway.management.workorder.dto;

import com.railway.management.workorder.model.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWorkOrderStatusRequest {

    @NotNull(message = "新状态不能为空")
    private WorkOrderStatus newStatus;
}