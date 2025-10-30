﻿package com.railway.management.workorder.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("work_order_status_history")
public class WorkOrderStatusHistory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workOrderId;
    private WorkOrderStatus fromStatus;
    private WorkOrderStatus toStatus;
    private Long operatorId;
    private String operatorName;
    private String remark;
    private LocalDateTime createdAt;

}
