package com.railway.management.common.train.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.common.train.dto.TrainCreateDto;
import com.railway.management.common.train.dto.TrainDto;
import com.railway.management.common.train.dto.TrainImportFailureDto;
import com.railway.management.common.train.dto.TrainUpdateDto;
import com.railway.management.common.train.model.Train;
import com.railway.management.common.train.service.TrainService;
import com.railway.management.common.dto.ExcelImportResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "列车信息管理")
@RestController
@RequestMapping("/api/trains")
@RequiredArgsConstructor
public class TrainController {

    private final TrainService trainService;

    @Operation(summary = "创建列车")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainDto> createTrain(@Validated @RequestBody TrainCreateDto createDto) {
        return ResponseEntity.ok(trainService.createTrain(createDto));
    }

    @Operation(summary = "更新列车信息")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainDto> updateTrain(@Validated @RequestBody TrainUpdateDto updateDto) {
        return ResponseEntity.ok(trainService.updateTrain(updateDto));
    }

    @Operation(summary = "删除列车")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrain(@PathVariable Long id) {
        trainService.deleteTrain(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "获取列车详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TrainDto> getTrainById(@PathVariable Long id) {
        return ResponseEntity.ok(trainService.getTrainDtoById(id));
    }

    @Operation(summary = "分页查询列车列表")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IPage<TrainDto>> getTrainPage(Page<Train> page,
                                                        @RequestParam(required = false) String trainNumber,
                                                        @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(trainService.getTrainPage(page, trainNumber, departmentId));
    }

    @Operation(summary = "导入列车信息")
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExcelImportResult<TrainImportFailureDto>> importTrains(@RequestParam("file") MultipartFile file) throws IOException {
        ExcelImportResult<TrainImportFailureDto> result = trainService.importTrains(file.getInputStream());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "导出列车信息")
    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public void exportTrains(HttpServletResponse response,
                             @RequestParam(required = false) String trainNumber,
                             @RequestParam(required = false) Long departmentId) throws IOException {
        trainService.exportTrains(response, trainNumber, departmentId);
    }
}