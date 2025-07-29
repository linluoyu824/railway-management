package com.railway.management.equipment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.equipment.dto.AccessControlCreateDto;
import com.railway.management.equipment.dto.AccessControlLogCreateDto;
import com.railway.management.equipment.dto.AccessControlUpdateDto;
import com.railway.management.equipment.model.AccessControl;
import com.railway.management.equipment.model.AccessControlLog;
import com.railway.management.equipment.service.AccessControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/access-controls")
@Tag(name = "门禁管理", description = "提供门禁设备的增删改查及操作记录功能")
@RequiredArgsConstructor
public class AccessControlController {

    private final AccessControlService accessControlService;

    @PostMapping
    @Operation(summary = "创建门禁设备")
    public ResponseEntity<AccessControl> createAccessControl(@Valid @RequestBody AccessControlCreateDto createDto) {
        AccessControl newAccessControl = accessControlService.createAccessControl(createDto);
        return new ResponseEntity<>(newAccessControl, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "分页查询门禁设备列表")
    public ResponseEntity<IPage<AccessControl>> listAccessControls(
            @Parameter(description = "当前用户所在的部门路径", required = true) @RequestParam String departmentPath,
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页显示数量", example = "10") @RequestParam(defaultValue = "10") long size
    ) {
        IPage<AccessControl> page = new Page<>(current, size);
        IPage<AccessControl> resultPage = accessControlService.listAccessControls(page, departmentPath);
        return ResponseEntity.ok(resultPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取单个门禁设备详情")
    public ResponseEntity<AccessControl> getAccessControlById(@Parameter(description = "门禁设备ID", required = true) @PathVariable Long id) {
        AccessControl accessControl = accessControlService.getById(id);
        if (accessControl == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accessControl);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新门禁设备信息")
    public ResponseEntity<?> updateAccessControl(
            @Parameter(description = "门禁设备ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AccessControlUpdateDto updateDto) {
        if (!id.equals(updateDto.getId())) {
            return ResponseEntity.badRequest().body("路径ID与请求体ID不匹配");
        }
        try {
            AccessControl updated = accessControlService.updateAccessControl(updateDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除门禁设备")
    public ResponseEntity<Void> deleteAccessControl(@Parameter(description = "门禁设备ID", required = true) @PathVariable Long id) {
        accessControlService.deleteAccessControl(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logs")
    @Operation(summary = "记录门禁开关操作", description = "提供门禁开关记录接口")
    public ResponseEntity<AccessControlLog> recordAction(@Valid @RequestBody AccessControlLogCreateDto logDto) {
        AccessControlLog newLog = accessControlService.recordAction(logDto);
        return new ResponseEntity<>(newLog, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "查询指定门禁设备的操作记录")
    public ResponseEntity<IPage<AccessControlLog>> getLogs(
            @Parameter(description = "门禁设备ID", required = true) @PathVariable Long id,
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页显示数量", example = "10") @RequestParam(defaultValue = "10") long size
    ) {
        IPage<AccessControlLog> page = new Page<>(current, size);
        IPage<AccessControlLog> resultPage = accessControlService.getLogsByAccessControlId(page, id);
        return ResponseEntity.ok(resultPage);
    }
}