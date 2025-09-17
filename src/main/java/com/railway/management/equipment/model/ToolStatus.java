package com.railway.management.equipment.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.railway.management.common.enums.BaseEnum;
import lombok.Getter;

@Getter
public enum ToolStatus implements BaseEnum<Integer> {
    IN_STOCK(0, "在库"),
    IN_USE(1, "使用中"),
    SCRAPPED(2, "已报废");

    @EnumValue
    private final Integer value;
    private final String description;

    ToolStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}