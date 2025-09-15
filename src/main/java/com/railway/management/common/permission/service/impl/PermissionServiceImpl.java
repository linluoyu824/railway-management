package com.railway.management.common.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.railway.management.common.permission.dto.PermissionCreateDto;
import com.railway.management.common.permission.dto.PermissionUpdateDto;
import com.railway.management.common.permission.mapper.PermissionMapper;
import com.railway.management.common.permission.model.Permission;
import com.railway.management.common.permission.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Override
    public List<Permission> getAllPermissions() {
        return list(new QueryWrapper<Permission>().orderByDesc("created_at"));
    }

    @Override
    @Transactional
    public Permission createPermission(PermissionCreateDto createDto) {
        if (count(new QueryWrapper<Permission>().eq("code", createDto.getCode())) > 0) {
            throw new IllegalStateException("权限代码 '" + createDto.getCode() + "' 已存在");
        }
        Permission permission = new Permission();
        permission.setName(createDto.getName());
        permission.setCode(createDto.getCode());
        permission.setDescription(createDto.getDescription());
        save(permission);
        return permission;
    }

    @Override
    @Transactional
    public Permission updatePermission(PermissionUpdateDto updateDto) {
        Permission existing = getById(updateDto.getId());
        if (existing == null) {
            throw new IllegalArgumentException("权限ID " + updateDto.getId() + " 不存在");
        }
        // 此处省略了对code冲突的检查，可以根据业务需求添加
        existing.setName(updateDto.getName());
        existing.setCode(updateDto.getCode());
        existing.setDescription(updateDto.getDescription());
        updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        // TODO: 在实际项目中，应先检查此权限是否被任何角色使用
        if (!removeById(id)) {
            throw new IllegalArgumentException("权限ID " + id + " 不存在，无法删除");
        }
    }
}