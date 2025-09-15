package com.railway.management.equipment.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("access_permission_request")
public class AccessPermissionRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workOrderId;
    private Long accessControlId;
    private Long requesterId;
    private String requesterName;
    private Long approverId;
    private AccessPermissionRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}