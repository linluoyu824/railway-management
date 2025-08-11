package com.railway.management.common.equipment.dto;

import com.railway.management.common.equipment.model.EquipmentStatus;
import com.railway.management.common.equipment.model.Parameter;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EquipmentDetailDto {
    private Long id;
    private String name;
    private String type;
    private String serialNumber;
    private LocalDate purchaseDate;
    private EquipmentStatus status;
    private String guideDocumentPath;
    private Long departmentId;
    private Long adminUserId;

    /** 管理人员姓名 */
    private String adminUserName;

    /** 管理人员联系电话 */
    private String adminUserPhone;

    private List<Parameter> parameters;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}