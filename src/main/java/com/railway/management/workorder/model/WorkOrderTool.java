package com.railway.management.workorder.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("work_order_tool")
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderTool {
    private Long workOrderId;
    private Long toolId;
}