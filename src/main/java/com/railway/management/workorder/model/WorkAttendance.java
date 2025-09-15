package com.railway.management.workorder.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单出工记录
 */
@Data
@TableName("work_attendance")
public class WorkAttendance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的工单ID
     */
    private Long workOrderId;

    /**
     * 打卡用户ID
     */
    private Long userId;

    /**
     * 打卡用户名
     */
    private String username;

    /**
     * 打卡类型 (上班/下班)
     */
    private WorkAttendanceType attendanceType;

    /**
     * 打卡时间
     */
    private LocalDateTime attendanceTime;

    /**
     * 打卡地点描述 (可选)
     */
    private String location;
}