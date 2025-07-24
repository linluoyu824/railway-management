package com.railway.management.equipment.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.railway.management.user.model.User;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("equipments")
public class Equipment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备名称 */
    private String name;

    /** 设备型号 */
    private String type;

    /** 序列号/出厂编号 */
    @TableField("serial_number")
    private String serialNumber;

    /** 购置日期 */
    @TableField("purchase_date")
    private LocalDate purchaseDate;

    /** 设备状态 */
    @TableField("status")
    @EnumValue // 标记为枚举值，存储到数据库的是枚举名
    private EquipmentStatus status;

    /** 指导文档存储路径 */
    @TableField("guide_document_path")
    private String guideDocumentPath;

    /** 所属部门ID (用于数据隔离) */
    @TableField("department_id")
    private Long departmentId;

    /** 管理人员ID */
    @TableField("admin_user_id")
    private Long adminUserId;

    /** 管理人员 (业务对象，不直接映射数据库) */
    @TableField(exist = false)
    private User adminUser;

    /**
     * 设备参数列表
     * 使用JacksonTypeHandler将List<Parameter>序列化为JSON字符串存储在数据库的单个字段中
     */
    @TableField(value = "parameters", typeHandler = JacksonTypeHandler.class)
    private List<Parameter> parameters;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
}