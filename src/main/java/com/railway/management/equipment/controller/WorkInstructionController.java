package com.railway.management.equipment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.equipment.dto.WorkInstructionDto;
import com.railway.management.equipment.dto.WorkInstructionUpdateDto;
import com.railway.management.equipment.model.WorkInstruction;
import com.railway.management.equipment.service.WorkInstructionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "作业指导书管理")
@RestController
@RequestMapping("/api/work-instructions")
@RequiredArgsConstructor
public class WorkInstructionController {

    private final WorkInstructionService workInstructionService;

    @Operation(summary = "上传并创建作业指导书")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkInstructionDto> createWorkInstruction(
            @RequestParam String name,
            @RequestParam("file") MultipartFile file) throws IOException {
        WorkInstructionDto createdDto = workInstructionService.createWorkInstruction(name, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    @Operation(summary = "更新作业指导书名称")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkInstructionDto> updateWorkInstruction(
            @PathVariable Long id,
            @Validated @RequestBody WorkInstructionUpdateDto updateDto) {
        return ResponseEntity.ok(workInstructionService.updateWorkInstruction(id, updateDto));
    }

    @Operation(summary = "删除作业指导书")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWorkInstruction(@PathVariable Long id) {
        workInstructionService.deleteWorkInstruction(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "分页查询作业指导书")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IPage<WorkInstructionDto>> getWorkInstructionPage(Page<WorkInstruction> page, @RequestParam(required = false) String name) {
        return ResponseEntity.ok(workInstructionService.getWorkInstructionPage(page, name));
    }

    @Operation(summary = "获取作业指导书详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkInstructionDto> getWorkInstructionById(@PathVariable Long id) {
        return ResponseEntity.ok(workInstructionService.getWorkInstructionDtoById(id));
    }
}