package com.railway.managementsystem.department.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.PageUtil;
import com.railway.managementsystem.department.model.Department;
import com.railway.managementsystem.department.repository.DepartmentMapper;
import com.railway.managementsystem.department.service.DepartmentService;
import com.railway.managementsystem.user.dto.UserSimpleDto;
import com.railway.managementsystem.user.model.User;
import com.railway.managementsystem.user.repository.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 默认所有公共方法为只读事务，提高性能
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper DepartmentMapper;
    private final UserMapper UserMapper;

    /**
     * 获取完整的部门树形结构。
     * 使用Hutool工具类构建，代码简洁且高效。
     *
     * @return 部门树列表，通常只有一个根节点（或多个顶级部门）。
     */
    @Override
    public List<Tree<Long>> getDepartmentTree() {
        List<Department> allDepartments = DepartmentMapper.findAll();

        // 1. 配置树节点信息
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setWeightKey("id"); // 使用id排序
        treeNodeConfig.setDeep(3); // 可选：限制树的最大深度

        // 2. 构建树
        // 参数(数据列表, 根节点ID, 配置, 节点转换器)
        // 根节点ID为null，Hutool会自动寻找parent_id为null的节点作为顶级节点
        return TreeUtil.build(allDepartments, null, treeNodeConfig,
                (department, tree) -> {
                    tree.setId(department.getId());
                    tree.setParentId(department.getParent() != null ? department.getParent().getId() : null);
                    tree.setName(department.getName());
                    // 可以添加任何需要的额外属性到树节点中
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
    public Page<Department> listDepartments(PageUtil page) {
        return DepartmentMapper.getAllBy(page);
    }

    /**
     * 根据部门ID，分页查询该部门下的所有职员。
     *
     * @param departmentId 部门ID
     * @param page     分页参数
     * @return 职员信息的DTO分页结果
     */
    @Override
    public Page<UserSimpleDto> listUsersByDepartment(Long departmentId, PageUtil page) {
        // 验证部门是否存在，如果不存在则抛出异常
        if (!DepartmentMapper.existsById(departmentId)) {
            throw new EntityNotFoundException("Department not found with id: " + departmentId);
        }

        Page<User> usersPage = UserMapper.findByDepartmentId(departmentId, page);
        // 使用 Page.map 将 User 实体转换为 UserSimpleDto，避免暴露敏感信息
        return usersPage.map(this::convertToUserSimpleDto);
    }

    /**
     * 将User实体转换为UserSimpleDto。
     */
    private UserSimpleDto convertToUserSimpleDto(User user) {
        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmployeeId(user.getEmployeeId());
        dto.setJobTitle(user.getJobTitle());
        dto.setMobilePhone(user.getMaskedMobilePhone()); // 使用实体中已有的脱敏方法
        return dto;
    }
}