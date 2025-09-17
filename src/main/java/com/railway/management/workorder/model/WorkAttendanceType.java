package com.railway.management.workorder.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.railway.management.common.enums.BaseEnum;
import lombok.Getter;

@Getter
public enum WorkAttendanceType implements BaseEnum<Integer> {
    CLOCK_IN(1, "上班打卡"),
    CLOCK_OUT(2, "下班打卡");

    @EnumValue
    private final Integer value;
    private final String description;

    WorkAttendanceType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}