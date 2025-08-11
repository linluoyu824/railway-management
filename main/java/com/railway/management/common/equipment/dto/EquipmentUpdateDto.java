package com.railway.management.common.equipment.dto;

import com.railway.management.common.equipment.model.EquipmentStatus;
import com.railway.management.common.equipment.model.Parameter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class EquipmentUpdateDto {

    @NotNull(message = "设备ID不能为空")
    @Min(value = 1, message = "设备ID必须大于0")
    private Long id;

    @NotBlank(message = "设备名称不能为空")
    private String name;

    @NotBlank(message = "设备型号不能为空")
    private String type;

    @NotBlank(message = "序列号不能为空")
    private String serialNumber;

    private LocalDate purchaseDate;

    @NotNull(message = "设备状态不能为空")
    private EquipmentStatus status;

    // 管理人员ID，可以为null，表示未分配
    private Long adminUserId;

    private Long sectionId;

    private List<Parameter> parameters;
}