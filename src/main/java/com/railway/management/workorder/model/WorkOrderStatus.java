package com.railway.management.workorder.model;

/**
 * 工单状态枚举
 */
public enum WorkOrderStatus {
    /**
     * 草稿
     */
    DRAFT,
    /**
     * 审批中
     */
    IN_APPROVAL,
    /**
     * 审批通过
     */
    APPROVED,
    /**
     * 维修中
     */
    IN_PROGRESS,
    /**
     * 维修完成
     */
    REPAIR_COMPLETED,
    /**
     * 工单完成
     */
    WORK_ORDER_COMPLETED,
    /**
     * 已取消
     */
    CANCELLED
}