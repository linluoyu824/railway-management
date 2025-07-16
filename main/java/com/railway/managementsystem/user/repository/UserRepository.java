package com.railway.managementsystem.user.repository;

import cn.hutool.core.util.PageUtil;
import com.railway.managementsystem.user.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMapper extends JpaRepository<User, Long> {
    Page<User> findByDepartmentId(Long departmentId, PageUtil page);

    Optional<User> findByUsername(@NotBlank(message = "用户名不能为空") @Size(min = 3, max = 50, message = "用户名长度必须在3到50之间") String username);

    Optional<User> findByEmployeeId(@NotBlank(message = "员工工号不能为空") String employeeId);

    List<User> findByJobLevel(int jobLevel);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.employeeId = :employeeId")
    Optional<User> findByEmployeeIdWithPermissions(@Param("employeeId") String employeeId);

    Page<User> findByTeam(Long department, PageUtil page);

    Page<User> getAllBy(PageUtil page);
}