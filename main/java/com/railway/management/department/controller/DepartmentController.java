package com.railway.management.department.controller;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.railway.management.department.dto.DepartmentCreateDto;
import com.railway.management.department.model.Department;
import com.railway.management.department.service.DepartmentService;
import com.railway.management.user.dto.UserImportResultDto;
import com.railway.management.user.dto.UserSimpleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "部门管理", description = "提供部门的增删改查、树形结构、导入导出等功能")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取部门树形结构
     *
     * @return 部门树列表
     */
    @GetMapping("/tree")
    @Operation(summary = "获取部门树", description = "以树形结构返回所有部门信息，用于前端组件展示")
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
    @Operation(summary = "分页查询部门列表", description = "获取扁平化的部门列表，支持分页")
    public ResponseEntity<IPage<Department>> listDepartments(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页显示数量", example = "10") @RequestParam(defaultValue = "10") long size
    ) {
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
    @Operation(summary = "查询部门下的用户", description = "分页查询指定部门下的所有用户信息")
    public ResponseEntity<IPage<UserSimpleDto>> listUsersByDepartment(
            @Parameter(description = "部门的唯一ID", required = true) @PathVariable Long departmentId,
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页显示数量", example = "10") @RequestParam(defaultValue = "10") long size) {
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
    @Operation(summary = "批量导入部门", description = "通过上传Excel文件批量创建部门层级结构")
    public ResponseEntity<UserImportResultDto> importDepartments(
            @Parameter(description = "包含部门信息的Excel文件", required = true) @RequestParam("file") MultipartFile file
    ) {
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
    @Operation(summary = "下载部门导入模板", description = "下载用于批量导入部门的Excel模板文件")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        departmentService.downloadTemplate(response);
    }

    @PostMapping
    @Operation(summary = "创建新部门", description = "创建一个新的部门，可指定其父部门")
    @ApiResponse(responseCode = "201", description = "部门创建成功")
    @ApiResponse(responseCode = "400", description = "请求参数无效或部门已存在")
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody DepartmentCreateDto createDto) {
        Department newDepartment = departmentService.createDepartment(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDepartment);
    }


}