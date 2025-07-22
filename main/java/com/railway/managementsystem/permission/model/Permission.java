package com.railway.managementsystem.permission.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = "id")
@TableName("permissions")
public class Permission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限名称, e.g., "创建用户"
     */
    private String name;

    /**
     * 权限代码, e.g., "user:create"
     */
    private String code;

    /**
     * 权限描述
     */
    private String description;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
}