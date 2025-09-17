package com.railway.management.equipment.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.railway.management.common.enums.BaseEnum;
import lombok.Getter;

@Getter
public enum AccessPermissionRequestStatus implements BaseEnum<Integer> {
    PENDING(0, "待审批"),
    APPROVED(1, "已同意"),
    REJECTED(2, "已拒绝");

    @EnumValue
    private final Integer value;
    private final String description;

    AccessPermissionRequestStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}