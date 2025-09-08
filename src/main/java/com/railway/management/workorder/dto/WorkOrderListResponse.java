package com.railway.management.workorder.dto;

import com.railway.management.workorder.model.WorkOrder;
import com.railway.management.workorder.model.WorkOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Schema(description = "工单列表响应参数")
public class WorkOrderListResponse {
    @Schema(description = "工单ID")
    private Long id;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "工单状态")
    private WorkOrderStatus status;
    @Schema(description = "创建人")
    private String creatorName;
    @Schema(description = "维修人")
    private String assigneeNames;
    @Schema(description = "维修设备")
    private String equipmentName;
    @Schema(description = "设备作业指导书名称")
    private String workInstructionName;
    @Schema(description = "设备作业指导书URL")
    private String workInstructionUrl;
    @Schema(description = "工单关闭时间")
    private LocalDateTime completedAt;

    public WorkOrderListResponse(WorkOrder workOrder) {
        this.id = workOrder.getId();
        this.createdAt = workOrder.getCreatedAt();
        this.status = workOrder.getStatus();
        this.creatorName = workOrder.getCreatorName();
        this.assigneeNames = workOrder.getAssigneeNames();
        this.equipmentName = workOrder.getEquipmentName();
        this.workInstructionName = workOrder.getWorkInstructionName();
        this.workInstructionUrl = workOrder.getWorkInstructionUrl();
        this.completedAt = workOrder.getCompletedAt();
    }
}