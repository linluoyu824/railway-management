package com.railway.management.equipment.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tool_log")
public class ToolLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long toolId;
    private Long userId;
    private String username;
    private String action; // e.g., "BORROW", "RETURN"
    private LocalDateTime timestamp;
}