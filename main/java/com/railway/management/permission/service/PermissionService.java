package com.railway.management.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.railway.management.permission.dto.PermissionCreateDto;
import com.railway.management.permission.dto.PermissionUpdateDto;
import com.railway.management.permission.model.Permission;

import java.util.List;

/**
 * 权限管理服务接口
 */
public interface PermissionService extends IService<Permission> {
    /**
     * 获取所有权限列表
     * @return 权限实体列表
     */
    List<Permission> getAllPermissions();

    /**
     * 创建新权限
     * @param createDto 创建权限的数据传输对象
     * @return 创建后的权限实体
     */
    Permission createPermission(PermissionCreateDto createDto);

    /**
     * 更新权限信息
     * @param updateDto 更新权限的数据传输对象
     * @return 更新后的权限实体
     */
    Permission updatePermission(PermissionUpdateDto updateDto);

    /**
     * 根据ID删除权限
     * @param id 权限ID
     */
    void deletePermission(Long id);
}