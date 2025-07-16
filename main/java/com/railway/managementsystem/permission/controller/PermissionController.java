package com.railway.managementsystem.permission.controller;

import com.railway.managementsystem.role.model.Role;
import com.railway.managementsystem.permission.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Set<String>> getEmployeePermissions(@PathVariable String employeeId) {
        Set<String> permissions = permissionService.getPermissionsForEmployee(employeeId);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/roles/my-tenant")
    public ResponseEntity<Set<Role>> getTenantRoles() {
        Set<Role> roles = permissionService.getRolesForCurrentTenant();
        return ResponseEntity.ok(roles);
    }
}