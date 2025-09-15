package com.railway.management.equipment.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("equipment_parameter")
public class Parameter {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long equipmentId;
    private String name;
    private String value;
    private String unit;
}