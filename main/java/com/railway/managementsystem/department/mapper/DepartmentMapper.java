package com.railway.managementsystem.department.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.managementsystem.department.model.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}