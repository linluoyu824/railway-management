package com.railway.management.user.model;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.railway.management.department.model.Department;
import com.railway.management.role.model.Role;
import com.railway.management.position.model.Position;
import cn.hutool.extra.pinyin.PinyinUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"department", "position", "roles"}) // 排除关联对象，防止潜在的循环引用问题
@EqualsAndHashCode(of = "id") // 实体类仅通过ID判断相等性
@TableName("users") // 指定数据库表名
public class User {

    @TableId(type = IdType.AUTO) // 声明主键，并设置为自增
    private Long id;

    private String username;

    @TableField(select = false) // 查询时默认不返回密码字段，增加安全性
    private String password;

    @TableField("full_name") // 数据库字段名与属性名不一致时使用
    private String fullName;

    @TableField("employee_id")
    private String employeeId;

    @TableField("pinyin_code")
    private String pinyinCode;

    /**
     * 用户所属部门
     */
    @TableField(exist = false) // 表示此字段在数据库表中不存在，是用于业务逻辑的关联对象
    private Department department;
    
    @TableField("department_id") // 数据库中实际存储的是部门ID
    private Long departmentId;

    /**
     * 用户所属职位
     */
    @TableField(exist = false)
    private Position position;

    @TableField("position_id")
    private Long positionId;

    @TableField(exist = false)
    private Set<Role> roles = new HashSet<>();

    @TableField("driver_license_type")
    private DriverLicenseType driverLicenseType; // 使用枚举类型更安全

    @TableField("mobile_phone")
    private String mobilePhone;

    @TableField(value = "created_at", fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createdAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField("job_level")
    private Integer jobLevel; // 新增字段：员工职级

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updatedAt;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    // Getters and Setters 由 @Data (Lombok) 自动生成
    // 如果不使用 Lombok，请手动添加

    /**
     * 在实体持久化或更新之前，自动根据姓名生成拼音码。
     * 使用 Hutool 的 PinyinUtil 工具类。
     * 这是一个在实体类中使用工具类的好例子，因为它直接关系到实体自身数据的生成。
     */
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