package com.railway.management.workorder.dto;

import com.railway.management.workorder.model.WorkAttendanceType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RecordAttendanceRequest {

    @NotNull(message = "工单ID不能为空")
    private Long workOrderId;

    @NotEmpty(message = "出工人员ID列表不能为空")
    private List<Long> userIds;

    @NotNull(message = "打卡类型不能为空")
    private WorkAttendanceType attendanceType;

    private String location;
}