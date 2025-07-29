package com.railway.management.equipment.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("equipment_access_control")
public class AccessControl {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String location;
    private String ipAddress;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    private Long departmentId;
    private String departmentPath;
    @TableField("status")
    private AccessControlStatus status;
}