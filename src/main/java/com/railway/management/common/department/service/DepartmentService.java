package com.railway.management.common.department.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.railway.management.common.department.dto.DepartmentCreateDto;
import com.railway.management.common.department.model.Department;
import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.common.user.dto.UserSimpleDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DepartmentService extends IService<Department> {
    @Transactional
    Department createDepartment(DepartmentCreateDto createDto);

    String buildDepartmentPath(Long departmentId);

    /**
     * 获取部门树形结构
     */
    List<Tree<Long>> getDepartmentTree();

    /**
     * 分页查询部门列表
     */
    IPage<Department> listDepartments(IPage<Department> page);

    /**
     * 分页查询指定部门下的用户列表
     */
    IPage<UserSimpleDto> listUsersByDepartment(Long departmentId, IPage<UserSimpleDto> page);
    /**
     * 从Excel批量导入部门
     */
    ExcelImportResult importDepartments(InputStream inputStream);
    /**
     * 下载导入模板
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;
}