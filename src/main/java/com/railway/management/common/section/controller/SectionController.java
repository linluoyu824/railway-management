package com.railway.management.common.section.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.common.section.dto.SectionCreateDto;
import com.railway.management.common.section.dto.SectionImportDto;
import com.railway.management.common.section.dto.SectionImportFailureDto;
import com.railway.management.common.section.dto.SectionUpdateDto;
import com.railway.management.common.section.model.Section;
import com.railway.management.common.section.service.SectionService;
import com.railway.management.utils.ExcelResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/sections")
@Tag(name = "区段管理", description = "提供区段的增删改查功能")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    /**
     * 创建区段
     */
    @PostMapping
    @Operation(summary = "创建区段", description = "创建一个新的区段")
    public ResponseEntity<Section> createSection(@Valid @RequestBody SectionCreateDto createDto) {
        Section newSection = sectionService.createSection(createDto);
        return new ResponseEntity<>(newSection, HttpStatus.CREATED);
    }

    /**
     * 更新区段
     */
    @PutMapping
    @Operation(summary = "更新区段", description = "更新区段信息")
    public ResponseEntity<Section> updateSection(@Valid @RequestBody SectionUpdateDto updateDto) {
        Section updatedSection = sectionService.updateSection(updateDto);
        return ResponseEntity.ok(updatedSection);
    }

    /**
     * 获取区段列表（分页）
     */
    @GetMapping
    @Operation(summary = "分页查询区段列表", description = "获取区段列表，支持分页")
    public ResponseEntity<IPage<Section>> listSections(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页显示数量", example = "10") @RequestParam(defaultValue = "10") long size
    ) {
        IPage<Section> page = new Page<>(current, size);
        IPage<Section> sections = sectionService.listSections(page);
        return ResponseEntity.ok(sections);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取区段", description = "获取单个区段")
    public ResponseEntity<Section> listSections(
            @Parameter(description = "区段ID", example = "1") @PathVariable Long id
    ) {
        Section sections = sectionService.getById(id);
        return ResponseEntity.ok(sections);
    }

    @PostMapping("/import-batch")
    @Operation(summary = "批量导入区段", description = "通过上传Excel文件批量创建区段")
    public ResponseEntity<Object> importSections(
            @Parameter(description = "包含区段信息的Excel文件", required = true) @RequestParam("file") MultipartFile file,
            HttpServletResponse response
    ) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择要上传的文件。");
        }
        try {
            ExcelImportResult<SectionImportDto> result = sectionService.importSections(file.getInputStream());

            if (result.hasFailures()) {
                response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
                List<SectionImportFailureDto> failureDtos = new ArrayList<>();
                for (int i = 0; i < result.getFailedRows().size(); i++) {
                    SectionImportDto failedRow = result.getFailedRows().get(i);
                    SectionImportFailureDto failureDto = new SectionImportFailureDto();
                    // 复制所有字段
                    failureDto.setName(failedRow.getName());
                    failureDto.setDescription(failedRow.getDescription());
                    failureDto.setMileage(failedRow.getMileage());
                    failureDto.setStartPoint(failedRow.getStartPoint());
                    failureDto.setEndPoint(failedRow.getEndPoint());
                    failureDto.setManagerId(failedRow.getManagerId());
                    failureDto.setDepartmentId(failedRow.getDepartmentId());
                    failureDto.setFailureReason(result.getFailureReasons().get(i));
                    failureDtos.add(failureDto);
                }
                ExcelResponseUtils.writeFailedExcel(response, failureDtos, SectionImportFailureDto.class);
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
            } else {
                String successMessage = "全部 " + result.getSuccessCount() + " 条区段记录导入成功。";
                return ResponseEntity.ok(successMessage);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("导入过程中发生错误：" + e.getMessage());
        }
    }


    @GetMapping("/template")
    @Operation(summary = "下载区段导入模板", description = "下载用于批量导入区段的Excel模板文件")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        sectionService.downloadTemplate(response);
    }

}