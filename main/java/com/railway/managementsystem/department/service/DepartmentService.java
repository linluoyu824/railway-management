package com.railway.managementsystem.department.service;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.PageUtil;
import com.railway.managementsystem.department.model.Department;
import com.railway.managementsystem.user.dto.UserSimpleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {
    List<Tree<Long>> getDepartmentTree();

    Page<Department> listDepartments(PageUtil page);

    Page<UserSimpleDto> listUsersByDepartment(Long departmentId, PageUtil page);
}
