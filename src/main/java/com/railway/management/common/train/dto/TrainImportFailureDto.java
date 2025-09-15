package com.railway.management.common.train.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TrainImportFailureDto extends TrainImportDto {
    private String failureReason;

    public TrainImportFailureDto(TrainImportDto importDto, String failureReason) {
        if (importDto != null) {
            this.setTrainNumber(importDto.getTrainNumber());
            this.setModel(importDto.getModel());
            this.setDepartmentName(importDto.getDepartmentName());
        }
        this.failureReason = failureReason;
    }
}