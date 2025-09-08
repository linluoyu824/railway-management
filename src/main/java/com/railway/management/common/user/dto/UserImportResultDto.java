package com.railway.management.common.user.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserImportResultDto {
    private int successCount = 0;
    private int failureCount = 0;
    private List<String> failureDetails = new ArrayList<>();
    private List<String> successDetails = new ArrayList<>();

    public void addFailureDetail(String detail) {
        this.failureDetails.add(detail);
    }

    public void addSuccessDetail(String detail) {
        this.successDetails.add(detail);
    }

}