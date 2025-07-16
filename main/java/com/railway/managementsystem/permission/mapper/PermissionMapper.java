package com.railway.managementsystem.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.managementsystem.permission.model.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    // 复杂的查询可以写在这里，或者在XML中
}