package com.railway.managementsystem.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.railway.managementsystem.permission.model.Permission;
import com.railway.managementsystem.permission.service.PermissionService;
import com.railway.managementsystem.role.model.Role;
import com.railway.managementsystem.user.mapper.UserMapper;
import com.railway.managementsystem.user.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final UserMapper userMapper;

    public PermissionServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 获取指定员工的功能权限列表（返回权限代码）。
     * 在多租户架构下，权限是根据用户所属的角色集合计算得出的，
     * 并且这些角色都必须属于用户所在的租户（部门）。
     *
     * 注意：此方法现在只依赖于用户已分配的角色，不再通过 jobLevel 进行硬编码映射，
     * 从而实现了灵活的权限配置。
     *
     * @param employeeId 员工工号
     * @return 一组权限代码，例如 ["user:create", "report:view"]
     */
    @Override
    public Set<String> getPermissionsForEmployee(String employeeId) {
        // 1. 使用高效的查询获取用户、角色和权限信息
        User user = userMapper.selectOne(Wrappers.query(User.class).eq("employeeId",employeeId));;

        // 2. 直接从用户关联的角色中提取所有权限
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    /**
     * 获取当前登录用户（租户）可用于分配的所有角色列表。
     * 这是实现灵活配置的关键，部门管理员只能看到并管理自己部门的角色。
     * @return 当前租户下的角色列表
     */
    @Override
    public Set<Role> getRolesForCurrentTenant() {
        // 1. 获取当前登录用户的信息
        // 实际项目中，用户信息通常在登录时存入 SecurityContext
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userMapper.selectOne(Wrappers.query(User.class).eq("employeeId",currentUsername));

        if (currentUser.getDepartment() == null) {
            // 如果是超级管理员或无部门用户，可以定义不同逻辑，例如返回所有角色
            // 这里为简化，假设所有用户都有部门
            throw new IllegalStateException("Current user does not belong to any department.");
        }

        // 2. 获取用户所属的部门ID (租户ID)
        Long tenantId = currentUser.getDepartment().getId();

        // 3. 从用户的所有角色中提取
        // 在一个更复杂的系统中，这里会调用 roleRepository.findByDepartmentId(tenantId)
        // 但为了演示，我们假设用户所拥有的角色必然属于其部门
        return currentUser.getRoles().stream()
                .filter(role -> role.getDepartment() != null && role.getDepartment().getId().equals(tenantId))
                .collect(Collectors.toSet());
    }

}