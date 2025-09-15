package com.railway.management.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.railway.management.common.department.model.Department;
import com.railway.management.common.department.service.DepartmentService;
import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.common.train.dto.TrainImportDto;
import com.railway.management.common.train.dto.TrainImportFailureDto;
import com.railway.management.common.train.model.Train;
import com.railway.management.common.train.model.TrainStatus;
import com.railway.management.common.train.service.TrainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class TrainImportListener implements ReadListener<TrainImportDto> {

    private final TrainService trainService;
    private final ExcelImportResult<TrainImportFailureDto> result;
    private final Map<String, Long> departmentCache;

    public TrainImportListener(TrainService trainService, DepartmentService departmentService, ExcelImportResult<TrainImportFailureDto> result) {
        this.trainService = trainService;
        this.result = result;
        // Cache all departments to avoid repeated DB queries in a loop
        this.departmentCache = departmentService.list().stream()
                .collect(Collectors.toMap(Department::getName, Department::getId, (existing, replacement) -> existing));
    }

    @Override
    public void invoke(TrainImportDto data, AnalysisContext context) {
        validate(data).ifPresentOrElse(
            // On validation failure
            failureReason -> result.addFailure(new TrainImportFailureDto(data, failureReason),failureReason),
            // On validation success
            () -> {
                Long departmentId = departmentCache.get(data.getDepartmentName());
                Train train = new Train();
                train.setTrainNumber(data.getTrainNumber());
                train.setModel(data.getModel());
                train.setDepartmentId(departmentId);
                train.setStatus(TrainStatus.IN_SERVICE);
                trainService.save(train);
                result.incrementSuccessCount(1);
            }
        );
    }

    /**
     * Validates the data and returns an Optional containing the failure reason.
     *
     * @param data The DTO to validate.
     * @return An Optional with the reason if validation fails, otherwise an empty Optional.
     */
    private Optional<String> validate(TrainImportDto data) {
        if (!StringUtils.hasText(data.getTrainNumber())) {
            return Optional.of("列车号不能为空");
        }
        if (!StringUtils.hasText(data.getModel())) {
            return Optional.of("列车型号不能为空");
        }
        if (!StringUtils.hasText(data.getDepartmentName())) {
            return Optional.of("所属部门不能为空");
        }

        // Business validation
        if (!departmentCache.containsKey(data.getDepartmentName())) {
            return Optional.of("所属部门 '" + data.getDepartmentName() + "' 不存在");
        }
        if (trainService.exists(new QueryWrapper<Train>().eq("train_number", data.getTrainNumber()))) {
            return Optional.of("列车号 '" + data.getTrainNumber() + "' 已存在");
        }

        return Optional.empty();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("列车数据导入完成. 成功: {} 条, 失败: {} 条", result.getSuccessCount(), result.getFailedRows().size());
    }
}