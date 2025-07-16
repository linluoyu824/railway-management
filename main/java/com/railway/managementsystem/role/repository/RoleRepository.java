package com.railway.managementsystem.role.repository;

import com.railway.managementsystem.role.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByDepartmentId(Long departmentId);
    Optional<Role> findByIdAndDepartmentId(Long id, Long departmentId);
}