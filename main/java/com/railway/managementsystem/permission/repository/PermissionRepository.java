package com.railway.managementsystem.permission.repository;

import com.railway.managementsystem.permission.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}