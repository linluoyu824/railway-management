package com.railway.management.department.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.railway.management.department.dto.DepartmentCreateDto;
import com.railway.management.department.model.Department;
import com.railway.management.user.dto.UserImportResultDto;
import com.railway.management.user.dto.UserSimpleDto;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DepartmentService {

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
    UserImportResultDto importDepartments(InputStream inputStream);

    /**
     * 下载部门导入模板
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;

    /**
     * 创建新部门
     * @param createDto 部门创建信息
     * @return 创建后的部门实体
     */
    Department createDepartment(DepartmentCreateDto createDto);

    /**
     * 获取部门路径
     * @param id
     * @return
     */
    String buildDepartmentPath(Long id);
}