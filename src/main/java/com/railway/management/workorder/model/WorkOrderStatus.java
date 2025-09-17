package com.railway.management.workorder.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.railway.management.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 工单状态枚举
 */
@Getter
public enum WorkOrderStatus implements BaseEnum<Integer> {
    /**
     * 草稿
     */
    DRAFT(0, "草稿"),

    /**
     * 审批中
     */
    IN_APPROVAL(10, "审批中"),

    /**
     * 审批通过 (待开工)
     */
    APPROVED(20, "审批通过"),

    /**
     * 维修中
     */
    IN_PROGRESS(30, "维修中"),

    /**
     * 维修完成 (待验收)
     */
    REPAIR_COMPLETED(40, "维修完成"),

    /**
     * 工单完成 (已关闭)
     */
    WORK_ORDER_COMPLETED(50, "工单完成");

    @EnumValue
    private final Integer value;
    private final String description;

    WorkOrderStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}