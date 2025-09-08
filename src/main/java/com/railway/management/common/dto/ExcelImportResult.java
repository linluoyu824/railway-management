package com.railway.management.common.dto;

import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ExcelImportResult<T> {
    private final List<T> failedRows = new ArrayList<>();
    private final List<String> failureReasons = new ArrayList<>();
    private int successCount = 0;

    public void addFailure(T row, String reason) {
        failedRows.add(row);
        failureReasons.add(reason);
    }

    public void incrementSuccessCount(int count) {
        this.successCount += count;
    }

    public boolean hasFailures() {
        return !failedRows.isEmpty();
    }
}