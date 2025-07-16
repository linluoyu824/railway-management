package com.railway.managementsystem.user.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserImportResultDto {
    private int successCount = 0;
    private int failureCount = 0;
    private List<String> errorMessages = new ArrayList<>();

    public void addSuccess() {
        this.successCount++;
    }

    public void addFailure(int rowNum, String message) {
        this.failureCount++;
        this.errorMessages.add(String.format("Row %d: %s", rowNum, message));
    }
}