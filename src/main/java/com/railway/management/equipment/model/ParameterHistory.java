package com.railway.management.equipment.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("equipment_parameter_history")
public class ParameterHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long equipmentId;
    private Long parameterId;
    private String parameterName;
    private String oldValue;
    private String newValue;
    private Long updatedBy;
    private String updatedByName;
    private Long workOrderId;
    private LocalDateTime updateTime;
}