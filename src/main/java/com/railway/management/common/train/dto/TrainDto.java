package com.railway.management.common.train.dto;

import com.railway.management.common.train.model.TrainStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrainDto {
    private Long id;
    private String trainNumber;
    private String model;
    private TrainStatus status;
    private Long departmentId;
    private String departmentName; // For better display
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}