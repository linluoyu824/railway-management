package com.railway.management.common.permission.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.common.permission.role.model.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    // 复杂的查询可以写在这里，或者在XML中
}