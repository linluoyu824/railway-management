package com.railway.management.common.permission.position.model;

import com.baomidou.mybatisplus.annotation.*;
import com.railway.management.common.department.model.Department;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("positions")
public class Position {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField("department_id")
    private Long departmentId;

    @TableField(exist = false)
    private Department department;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
}