package com.railway.management.permission.service;

import com.railway.management.role.model.Role;

import java.util.Set;

public interface PermissionService {
    Set<String> getPermissionsForEmployee(String employeeId);

    Set<Role> getRolesForCurrentTenant();
}
