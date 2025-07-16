package com.railway.managementsystem.position.repository;

import com.railway.managementsystem.position.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.railway.managementsystem.department.model.Department;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    Optional<Position> findByNameAndDepartment(String name, Department department);
}