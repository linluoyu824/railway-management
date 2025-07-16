package com.railway.managementsystem.department.model;

import com.railway.managementsystem.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "部门层级不能为空")
    @Column(name = "level", nullable = false)
    private Integer level;

    /**
     * 所属上级部门
     * 这是一个自关联，用于构建部门的树形结构。
     * FetchType.LAZY 表示在需要时才加载父部门信息，提高性能。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") // 外键列
    private Department parent;

    /**
     * 下级部门列表
     * mappedBy = "parent" 表示这个关系的维护方是子部门的 "parent" 字段。
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Department> children = new HashSet<>();

    /**
     * 该部门下的所有用户
     */
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    // 可以在此添加审计字段（createdAt, createdBy等）以保持项目一致性
    // ...

}