package com.railway.management.department.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.department.model.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}