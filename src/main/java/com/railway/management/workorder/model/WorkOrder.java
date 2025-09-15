package com.railway.management.workorder.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("work_order")
public class WorkOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工单类型
     */
    private WorkOrderType type;

    // 设备信息
    private Long equipmentId;
    private String equipmentName;

    // 问题描述
    private String description;

    // 发起人信息 (班组长)
    private Long creatorId;
    private String creatorName;

    // 维修人员信息
    private String assigneeIds; // 存储逗号分隔的ID字符串
    private String assigneeNames; // 存储逗号分隔的Name字符串

    // 关联的工作指导文件
    private Long workInstructionId;
    private String workInstructionName;
    private String workInstructionUrl;

    // 维修工具信息 (e.g., "万用表, 扳手")
    private String tools;

    // 工单状态
    private WorkOrderStatus status;

    // 时间戳
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}