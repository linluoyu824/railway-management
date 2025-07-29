package com.railway.management.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.permission.model.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限 Mapper 接口
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}