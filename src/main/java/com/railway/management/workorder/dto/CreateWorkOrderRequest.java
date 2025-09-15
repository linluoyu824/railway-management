package com.railway.management.workorder.dto;

import com.railway.management.workorder.model.WorkOrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateWorkOrderRequest {

    @NotNull(message = "工单类型不能为空")
    private WorkOrderType type;

    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    @NotEmpty(message = "维修人员ID列表不能为空")
    private List<Long> assigneeIds;


    @NotEmpty(message = "维修人员ID列表不能为空")
    private List<Long> toolIds;

    @NotBlank(message = "问题描述不能为空")
    private String description;
}