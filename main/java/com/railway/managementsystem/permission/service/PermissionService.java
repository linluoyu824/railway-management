package com.railway.managementsystem.permission.service;

import com.railway.managementsystem.role.model.Role;

import java.util.Set;

public interface PermissionService {
    Set<String> getPermissionsForEmployee(String employeeId);

    Set<Role> getRolesForCurrentTenant();
}
