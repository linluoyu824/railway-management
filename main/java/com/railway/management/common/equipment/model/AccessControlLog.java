package com.railway.management.common.equipment.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("equipment_access_control_log")
public class AccessControlLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long accessControlId;
    private Long userId;
    @TableField("`action`") // `action` 是SQL保留关键字，需要转义
    private AccessControlAction action;
    private LocalDateTime timestamp;
    private boolean success;
    private String remark;
}