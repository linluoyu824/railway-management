package com.railway.management.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.permission.model.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}