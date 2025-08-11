package com.railway.management.common.section.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("section")
public class Section {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private BigDecimal mileage; // 区段里程

    private String startPoint; // 起始点

    private String endPoint; // 终点

    private Long managerId;

    private Long departmentId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public Section(String name, String description, Long managerId, Long departmentId) {
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.departmentId = departmentId;
    }

    public Section() {

    }
}
