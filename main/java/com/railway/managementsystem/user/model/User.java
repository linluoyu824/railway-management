package com.railway.managementsystem.user.model;

import cn.hutool.core.util.StrUtil;
import com.railway.managementsystem.department.model.Department;
import com.railway.managementsystem.role.model.Role;
import com.railway.managementsystem.position.model.Position;
import cn.hutool.extra.pinyin.PinyinUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

//@Data // 使用更具体的注解替换@Data，以避免潜在的性能和懒加载问题
@Getter
@Setter
@ToString(exclude = {"department", "position", "roles"}) // 排除关联对象，防止潜在的循环引用问题
@EqualsAndHashCode(of = "id") // 实体类仅通过ID判断相等性
@Entity
@EntityListeners(AuditingEntityListener.class) // 启用JPA审计监听器
@Filter(name = "tenantFilter")
@Table(name = "users") // 使用 "users" 作为表名，因为 "user" 在某些数据库中是关键字
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Column(nullable = false) // 密码字段通常不限制长度，因为哈希后的长度是固定的
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @NotBlank(message = "员工工号不能为空")
    @Size(max = 20)
    @Column(name = "employee_id", nullable = false, unique = true, length = 20)
    private String employeeId;

    @Column(name = "pinyin_code", length = 50)
    private String pinyinCode;

    /**
     * 用户所属部门
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id") // 在users表中创建的外键列
    private Department department;

    /**
     * 用户所属职位
     * 通过 ManyToOne 关联到独立的 Position 实体，替代了原来的 jobTitle 字段
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id") // 在users表中创建的外键列
    private Position position;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();


    @Enumerated(EnumType.STRING)
    @Column(name = "driver_license_type", length = 20)
    private DriverLicenseType driverLicenseType; // 使用枚举类型更安全

    @NotBlank(message = "手机号不能为空")
    @Size(max = 20)
    @Column(name = "mobile_phone", nullable = false, unique = true, length = 20)
    private String mobilePhone;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @CreatedBy // 由Spring Data JPA自动填充
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    @NotNull(message = "员工职级不能为空")
    @Column(name = "job_level")
    private Integer jobLevel; // 新增字段：员工职级

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @LastModifiedBy // 由Spring Data JPA自动填充
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    // Getters and Setters 由 @Data (Lombok) 自动生成
    // 如果不使用 Lombok，请手动添加

    /**
     * 在实体持久化或更新之前，自动根据姓名生成拼音码。
     * 使用 Hutool 的 PinyinUtil 工具类。
     * 这是一个在实体类中使用工具类的好例子，因为它直接关系到实体自身数据的生成。
     */
    @PrePersist
    @PreUpdate
    public void generatePinyinCode() {
        if (StrUtil.isNotBlank(this.fullName)) {
            // 例如，将 "张三" 转换为 "zhangsan"
            this.pinyinCode = PinyinUtil.getPinyin(this.fullName, "");
        }
    }

    /**
     * 提供一个脱敏的手机号码，用于日志或前端安全展示。
     * 这是一个瞬时(transient)方法，它的返回值不会被持久化到数据库。
     * @return 脱敏后的手机号，例如 "138****1234"
     */
    public String getMaskedMobilePhone() {
        return StrUtil.hide(this.mobilePhone, 3, 7);
    }
}

// 建议在单独的文件中定义，这里为方便展示写在一起
public enum DriverLicenseType {
    C1, C2, A1, A2, B1, B2
}