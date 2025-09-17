package com.railway.management.workorder.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("work_order_assignee")
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderAssignee {
    private Long workOrderId;
    private Long userId;
}