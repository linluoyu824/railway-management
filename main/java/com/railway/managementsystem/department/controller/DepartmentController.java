package com.railway.managementsystem.department.controller;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.railway.managementsystem.department.dto.DepartmentCreateDto;
import com.railway.managementsystem.department.model.Department;
import com.railway.managementsystem.department.service.DepartmentService;
import com.railway.managementsystem.user.dto.UserImportResultDto;
import com.railway.managementsystem.user.dto.UserSimpleDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.io.IOException;
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
     * @param current 当前页码
     * @param size    每页数量
     * @return 部门分页数据
     */
    @GetMapping
    public ResponseEntity<IPage<Department>> listDepartments(@RequestParam(defaultValue = "1") long current, @RequestParam(defaultValue = "10") long size) {
        IPage<Department> page = new Page<>(current, size);
        IPage<Department> departments = departmentService.listDepartments(page);
        return ResponseEntity.ok(departments);
    }

    /**
     * 分页查询指定部门下的用户列表
     *
     * @param departmentId 部门ID
     * @param current      当前页码
     * @param size         每页数量
     * @return 用户分页数据
     */
    @GetMapping("/{departmentId}/users")
    public ResponseEntity<IPage<UserSimpleDto>> listUsersByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "1") long current, @RequestParam(defaultValue = "10") long size) {
        IPage<UserSimpleDto> page = new Page<>(current, size);
        IPage<UserSimpleDto> users = departmentService.listUsersByDepartment(departmentId, page);
        return ResponseEntity.ok(users);
    }

    /**
     * 批量导入部门
     * @param file Excel文件
     * @return 导入结果，包含成功和失败的数量及信息
     */
    @PostMapping("/import-batch")
    public ResponseEntity<UserImportResultDto> importDepartments(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            UserImportResultDto result = departmentService.importDepartments(file.getInputStream());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            // 在实际项目中，应使用全局异常处理器来处理此类异常
            throw new RuntimeException("处理部门导入Excel文件失败", e);
        }
    }

    /**
     * 下载部门导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        departmentService.downloadTemplate(response);
    }

    // TODO:  Add more endpoints for creating, updating, and deleting departments
    // Example for creating a department (requires a DepartmentCreateDto):

    @PostMapping
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody DepartmentCreateDto createDto) {
        Department newDepartment = departmentService.createDepartment(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDepartment);
    }


}