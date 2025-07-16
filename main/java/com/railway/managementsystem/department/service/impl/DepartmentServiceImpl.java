package com.railway.managementsystem.department.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.railway.managementsystem.department.mapper.DepartmentMapper;
import com.railway.managementsystem.department.model.Department;
import com.railway.managementsystem.department.service.DepartmentService;
import com.railway.managementsystem.user.dto.UserSimpleDto;
import com.railway.managementsystem.user.mapper.UserMapper;
import com.railway.managementsystem.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.zip.DataFormatException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;

    /**
     * 获取完整的部门树形结构。
     * 使用Hutool工具类构建，代码简洁且高效。
     *
     * @return 部门树列表，通常只有一个根节点（或多个顶级部门）。
     */
    @Override
    public List<Tree<Long>> getDepartmentTree() {
        List<Department> allDepartments = departmentMapper.selectList(null);

        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setWeightKey("id");
        treeNodeConfig.setDeep(3);

        // 2. 构建树
        // 参数(数据列表, 根节点ID, 配置, 节点转换器)
        // 根节点ID为null，Hutool会自动寻找parent_id为null的节点作为顶级节点
        return TreeUtil.build(allDepartments, null, treeNodeConfig,
                (department, tree) -> {
                    tree.setId(department.getId());
                    tree.setParentId(department.getParent() != null ? department.getParent().getId() : null);
                    tree.setName(department.getName());
                    tree.putExtra("level", department.getLevel());
                });
    }

    /**
     * 分页查询部门列表。
     *
     * @param page 分页参数 (e.g., page, size, sort)
     * @return 部门的分页结果
     */
    @Override
    public IPage<Department> listDepartments(IPage<Department> page) {
        return departmentMapper.selectPage(page, null);
    }

    /**
     * 根据部门ID，分页查询该部门下的所有职员。
     *
     * @param departmentId 部门ID
     * @param page     分页参数
     * @return 职员信息的DTO分页结果
     */
    @Override
    public IPage<UserSimpleDto> listUsersByDepartment(Long departmentId, IPage<UserSimpleDto> page) {
        if (departmentMapper.selectById(departmentId) == null) {
            throw new DataFormatException("Department not found with id: " + departmentId);
        }
        // This assumes you have a custom method in UserMapper to do this.
        // Let's define it in UserMapper.xml for clarity.
        return userMapper.selectUsersByDepartmentPage(page, departmentId);
    }
}