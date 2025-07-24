package com.railway.management.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.railway.management.permission.dto.PermissionCreateDto;
import com.railway.management.permission.dto.PermissionUpdateDto;
import com.railway.management.permission.model.Permission;

import java.util.List;

public interface PermissionService extends IService<Permission> {
    /**
     * 获取所有权限列表
     */
    List<Permission> getAllPermissions();

    /**
     * 创建新权限
     */
    Permission createPermission(PermissionCreateDto createDto);

    /**
     * 更新权限信息
     */
    Permission updatePermission(PermissionUpdateDto updateDto);

    /**
     * 根据ID删除权限
     */
    void deletePermission(Long id);
}