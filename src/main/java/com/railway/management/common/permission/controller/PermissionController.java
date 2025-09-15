package com.railway.management.common.permission.controller;

import com.railway.management.common.permission.dto.PermissionCreateDto;
import com.railway.management.common.permission.dto.PermissionUpdateDto;
import com.railway.management.common.permission.model.Permission;
import com.railway.management.common.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@Tag(name = "权限管理", description = "提供权限的增删改查功能")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(summary = "获取所有权限列表", description = "返回系统中定义的所有权限")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    @PostMapping
    @Operation(summary = "创建新权限", description = "创建一个新的权限项，如 'user:create'")
    public ResponseEntity<?> createPermission(@Valid @RequestBody PermissionCreateDto createDto) {
        try {
            Permission newPermission = permissionService.createPermission(createDto);
            return new ResponseEntity<>(newPermission, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新权限信息", description = "更新指定ID的权限信息")
    public ResponseEntity<?> updatePermission(
            @Parameter(description = "要更新的权限ID", required = true) @PathVariable Long id,
            @Valid @RequestBody PermissionUpdateDto updateDto) {
        if (!id.equals(updateDto.getId())) {
            return ResponseEntity.badRequest().body("路径ID与请求体ID不匹配");
        }
        try {
            Permission updatedPermission = permissionService.updatePermission(updateDto);
            return ResponseEntity.ok(updatedPermission);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限", description = "根据ID删除一个权限")
    public ResponseEntity<Void> deletePermission(
            @Parameter(description = "要删除的权限ID", required = true) @PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}