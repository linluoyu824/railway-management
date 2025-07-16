package com.railway.managementsystem.department.repository;

import cn.hutool.core.util.PageUtil;
import com.railway.managementsystem.department.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DepartmentMapper extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    Optional<Department> findByNameAndParent(String name, Department parent);
    Optional<Department> findByNameAndParentIsNull(String name);

    Page<Department> getAllBy(PageUtil page);
}