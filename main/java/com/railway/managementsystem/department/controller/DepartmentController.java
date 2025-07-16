package com.railway.managementsystem.department.controller;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.PageUtil;
import com.railway.managementsystem.department.model.Department;
import com.railway.managementsystem.department.service.DepartmentService;
import com.railway.managementsystem.user.dto.UserSimpleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取部门树形结构
     *
     * @return 部门树列表
     */
    @GetMapping("/tree")
    public ResponseEntity<List<Tree<Long>>> getDepartmentTree() {
        List<Tree<Long>> departmentTree = departmentService.getDepartmentTree();
        return ResponseEntity.ok(departmentTree);
    }

    /**
     * 分页查询部门列表
     *
     * @param page 分页参数
     * @return 部门分页数据
     */
    @GetMapping
    public ResponseEntity<Page<Department>> listDepartments(PageUtil page) {
        Page<Department> departments = departmentService.listDepartments(page);
        return ResponseEntity.ok(departments);
    }

    /**
     * 分页查询指定部门下的用户列表
     *
     * @param departmentId 部门ID
     * @param page     分页参数
     * @return 用户分页数据
     */
    @GetMapping("/{departmentId}/users")
    public ResponseEntity<Page<UserSimpleDto>> listUsersByDepartment(
            @PathVariable Long departmentId,
            PageUtil page) {
        Page<UserSimpleDto> users = departmentService.listUsersByDepartment(departmentId, page);
        return ResponseEntity.ok(users);
    }

    // TODO:  Add more endpoints for creating, updating, and deleting departments
    // Example for creating a department (requires a DepartmentCreateDto):
    /*
    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody @Valid DepartmentCreateDto createDto) {
        Department newDepartment = departmentService.createDepartment(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDepartment);
    }
    */

}