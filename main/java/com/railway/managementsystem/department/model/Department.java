package com.railway.managementsystem.department.model;

import com.baomidou.mybatisplus.annotation.*;
import com.railway.managementsystem.user.model.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
// 在ToString中排除关联对象，防止因循环引用导致的StackOverflowError
@ToString(exclude = {"parent", "children", "users"})
@EqualsAndHashCode(of = "id") // 实体类仅通过ID判断相等性
@TableName("departments")
public class Department {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer level;

    /**
     * 所属上级部门
     */
    @TableField(exist = false)
    private Department parent;

    @TableField("parent_id")
    private Long parentId;

    /**
     * 下级部门列表
     */
    @TableField(exist = false)
    private Set<Department> children = new HashSet<>();

    /**
     * 该部门下的所有用户
     */
    @TableField(exist = false)
    private Set<User> users = new HashSet<>();

    // 可以在此添加审计字段（createdAt, createdBy等）以保持项目一致性
    // ...
    public Department(String name, Integer level, Department parent) {
        this.name = name;
        this.level = level;
        this.parent = parent;
        if (parent != null) {
            this.parentId = parent.getId();
        }
    }

}