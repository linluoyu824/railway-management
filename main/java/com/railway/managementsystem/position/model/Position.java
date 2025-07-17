package com.railway.managementsystem.position.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.railway.managementsystem.department.model.Department;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@TableName("positions")
public class Position {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    /**
     * 职位所属的部门 (租户)
     */
    @TableField(exist = false)
    private Department department;

    @TableField("department_id")
    private Long departmentId;
}