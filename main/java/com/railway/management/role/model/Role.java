package com.railway.management.role.model;

import com.baomidou.mybatisplus.annotation.*;
import com.railway.management.department.model.Department;
import com.railway.management.permission.model.Permission;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@TableName("roles")
public class Role {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name; // 角色名称, e.g., "系统管理员"

    private String code; // 角色代码, e.g., "ROLE_ADMIN"

    @TableField(exist = false)
    private Set<Permission> permissions = new HashSet<>();

    /**
     * 角色所属的部门 (租户)
     */
    @TableField(exist = false)
    private Department department;

    @TableField("department_id")
    private Long departmentId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
}