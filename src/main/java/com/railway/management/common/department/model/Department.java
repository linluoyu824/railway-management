package com.railway.management.common.department.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@TableName("departments")
public class Department {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer level;

    @TableField("parent_id")
    private Long parentId;

    @TableField(exist = false)
    private Department parent;

    @TableField(exist = false)
    private List<Department> children;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 用于导入逻辑的构造函数
     * @param name 部门名称
     * @param level 部门层级
     * @param parent 父部门
     */
    public Department(String name, int level, Department parent) {
        this.name = name;
        this.level = level;
        this.parent = parent;
        if (parent != null) {
            this.parentId = parent.getId();
        }
    }
}