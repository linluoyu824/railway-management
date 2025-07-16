package com.railway.managementsystem.department.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.railway.managementsystem.department.model.Department;
import com.railway.managementsystem.user.dto.UserSimpleDto;

import java.util.List;

public interface DepartmentService {
    List<Tree<Long>> getDepartmentTree();

    IPage<Department> listDepartments(IPage<Department> page);

    IPage<UserSimpleDto> listUsersByDepartment(Long departmentId, IPage<UserSimpleDto> page);
}
