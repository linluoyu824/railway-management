package com.railway.management.workorder.dto;

import com.railway.management.workorder.model.WorkOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "工单查询请求参数")
public class WorkOrderQueryRequest {
    @Schema(description = "开始时间", example = "2023-01-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2023-01-31T23:59:59")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    @Schema(description = "工单状态")
    private WorkOrderStatus status;

    @Schema(description = "创建人名称")
    private String creatorName;

    @Schema(description = "维修人名称")
    private String assigneeNames;
}