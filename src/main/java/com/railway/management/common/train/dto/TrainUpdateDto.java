package com.railway.management.common.train.dto;

import com.railway.management.common.train.model.TrainStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrainUpdateDto {
    @NotNull(message = "列车ID不能为空")
    private Long id;

    private String trainNumber;

    private String model;

    private TrainStatus status;

    private Long departmentId;
}