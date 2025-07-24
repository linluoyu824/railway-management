package com.railway.management.department.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.department.dto.DepartmentCreateDto;
import com.railway.management.department.dto.DepartmentImportDto;
import com.railway.management.department.mapper.DepartmentMapper;
import com.railway.management.department.model.Department;
import com.railway.management.department.service.DepartmentService;
import com.railway.management.user.dto.UserImportResultDto;
import com.railway.management.user.model.User;
import com.railway.management.user.dto.UserSimpleDto;
import com.railway.management.user.mapper.UserMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    private final UserMapper userMapper; // 注入 UserMapper

    @Override
    @Transactional
    public Department createDepartment(DepartmentCreateDto createDto) {
        // 1. 检查同级下是否存在同名部门
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", createDto.getName());
        if (createDto.getParentId() == null) {
            queryWrapper.isNull("parent_id");
        } else {
            queryWrapper.eq("parent_id", createDto.getParentId());
        }

        if (departmentMapper.selectCount(queryWrapper) > 0) {
            throw new IllegalStateException("在同一级别下已存在名为 '" + createDto.getName() + "' 的部门");
        }

        // 2. 创建并保存新部门
        Department department = new Department();
        department.setName(createDto.getName());
        department.setParentId(createDto.getParentId());

        // 3. 计算部门层级
        if (createDto.getParentId() != null) {
            Department parent = departmentMapper.selectById(createDto.getParentId());
            if (parent != null) {
                department.setLevel(parent.getLevel() + 1);
            } else {
                throw new IllegalArgumentException("父部门ID " + createDto.getParentId() + " 不存在");
            }
        } else {
            department.setLevel(1); // 根部门层级为1
        }

        departmentMapper.insert(department);
        return department;
    }

    @Override
    public List<Tree<Long>> getDepartmentTree() {
        // 1. 获取所有部门列表，可以根据需要添加排序
        List<Department> departments = departmentMapper.selectList(null);

        // 2. 将 Department 列表转换为 Hutool 的 TreeNode 列表
        List<TreeNode<Long>> nodes = departments.stream()
                .map(dept -> {
                    // TreeNode(ID, parentID, name, weight)
                    // 创建一个Map来存放所有自定义属性，然后通过构造函数一次性传入
                    // 这种方式更健壮，可以避免泛型类型推断问题
                    final Map<String, Object> extra = new HashMap<>();
                    extra.put("level", dept.getLevel());
                    extra.put("createdAt", dept.getCreatedAt());

                    // 使用带有extra参数的构造函数
                    return new TreeNode<>(dept.getId(), dept.getParentId(), dept.getName(), dept.getCreatedAt()).setExtra(extra);
                })
                .collect(Collectors.toList());

        // 3. 使用 TreeUtil 构建树，它会内部递归处理。根节点的 parentId 为 null
        return TreeUtil.build(nodes, null);
    }

    @Override
    public IPage<Department> listDepartments(IPage<Department> page) {
        // 使用 QueryWrapper 来添加排序条件，而不是直接操作 IPage 接口
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");
        return departmentMapper.selectPage(page, queryWrapper);
    }

    @Override
    public IPage<UserSimpleDto> listUsersByDepartment(Long departmentId, IPage<UserSimpleDto> page) {
        // 1. 创建用于查询 User 实体的新分页对象
        Page<User> userPage = new Page<>(page.getCurrent(), page.getSize());

        // 2. 构建查询条件，筛选指定部门的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department_id", departmentId);

        // 3. 执行分页查询，获取 IPage<User>
        IPage<User> resultPage = userMapper.selectPage(userPage, queryWrapper);

        // 4. 使用 IPage.convert() 方法安全地将 IPage<User> 转换为 IPage<UserSimpleDto>
        return resultPage.convert(user -> new UserSimpleDto(user.getId(), user.getFullName()));
    }

    @Override
    @Transactional
    public UserImportResultDto importDepartments(InputStream inputStream) {
        UserImportResultDto result = new UserImportResultDto();
        List<DepartmentImportDto> data;
        try {
            data = EasyExcel.read(inputStream, DepartmentImportDto.class, null).sheet().doReadSync();
        } catch (Exception e) {
            result.setFailureCount(1);
            result.addFailureDetail("Excel文件解析失败: " + e.getMessage());
            return result;
        }

        if (data == null || data.isEmpty()) {
            return result; // 返回空的成功结果
        }

        // 使用Map作为缓存，避免在同一次导入中重复查询数据库
        // Key: "parentId:departmentName", Value: Department对象
        Map<String, Department> processedDepartments = new HashMap<>();
        int newDepartmentsCreated = 0;
        int successRowCount = 0;

        for (DepartmentImportDto dto : data) {
            Long parentId = null;
            int level = 1;

            List<String> departmentNames = Arrays.asList(
                    dto.getLevelOneDepartment(),
                    dto.getLevelTwoDepartment(),
                    dto.getLevelThreeDepartment(),
                    dto.getLevelFourDepartment()
            );

            for (String deptName : departmentNames) {
                if (!StringUtils.hasText(deptName)) {
                    break; // 到达当前行的层级末端
                }

                String cacheKey = (parentId == null ? "null" : parentId) + ":" + deptName;
                Department department = processedDepartments.get(cacheKey);

                if (department == null) {
                    // 缓存未命中，查询数据库
                    QueryWrapper<Department> queryWrapper = new QueryWrapper<Department>().eq("name", deptName);
                    queryWrapper.eq(parentId != null, "parent_id", parentId);
                    queryWrapper.isNull(parentId == null, "parent_id");

                    department = departmentMapper.selectOne(queryWrapper);

                    if (department == null) {
                        // 数据库也不存在，则创建新部门
                        department = new Department();
                        department.setName(deptName);
                        department.setParentId(parentId);
                        department.setLevel(level);
                        departmentMapper.insert(department); // ID将自动填充
                        newDepartmentsCreated++;
                    }
                    processedDepartments.put(cacheKey, department);
                }

                // 为下一层级做准备
                parentId = department.getId();
                level++;
            }
            successRowCount++;
        }

        result.setSuccessCount(successRowCount);
        result.setFailureCount(data.size() - successRowCount);
        result.addSuccessDetail("处理完成，共创建了 " + newDepartmentsCreated + " 个新部门。");
        return result;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        String fileName = "部门导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 使用 URLEncoder 来处理中文文件名，防止乱码
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");

        try (ServletOutputStream outputStream = response.getOutputStream();
             ExcelWriter excelWriter = EasyExcel.write(outputStream).build()) {

            WriteSheet writeSheet = EasyExcel.writerSheet(0, "部门信息").head(DepartmentImportDto.class).build();
            // 传递一个空列表来解决 `write` 方法的引用不明确问题
            excelWriter.write(Collections.emptyList(), writeSheet);
        }


    }

}