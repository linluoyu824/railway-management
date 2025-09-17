package com.railway.management.workorder.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.railway.management.common.enums.BaseEnum;
import lombok.Getter;

@Getter
public enum WorkOrderType implements BaseEnum<Integer> {
    TRAINING(1, "培训工单"),
    OPERATION(2, "作业工单"),
    INSPECTION(3, "巡查工单");

    @EnumValue
    private final Integer value;
    private final String description;

    WorkOrderType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}