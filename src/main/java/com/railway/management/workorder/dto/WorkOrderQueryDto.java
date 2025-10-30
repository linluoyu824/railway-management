package com.railway.management.workorder.dto;

import com.railway.management.workorder.model.WorkOrderStatus;
import com.railway.management.workorder.model.WorkOrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "工单查询参数")
public class WorkOrderQueryDto {

    @Schema(description = "工单状态")
    private WorkOrderStatus status;

    @Schema(description = "工单类型")
    private WorkOrderType type;

    @Schema(description = "工单描述，支持模糊查询")
    private String description;

    @Schema(description = "创建人名称")
    private String creatorName;

    @Schema(description = "设备名称")
    private String equipmentName;

    @Schema(description = "创建开始时间")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @Schema(description = "创建结束时间")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;
}