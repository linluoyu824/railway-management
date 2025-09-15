package com.railway.management.equipment.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("equipment_inspection_log")
public class EquipmentInspectionLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private Long inspectorId;
    private String inspectorName;
    private LocalDateTime inspectionTime;
    private String remark;
}