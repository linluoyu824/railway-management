package com.railway.managementsystem.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.railway.managementsystem.department.dto.DepartmentImportDto;
import com.railway.managementsystem.department.mapper.DepartmentMapper;
import com.railway.managementsystem.department.model.Department;
import com.railway.managementsystem.user.dto.UserImportResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class DepartmentImportListener implements ReadListener<DepartmentImportDto> {

    private final DepartmentMapper departmentMapper;
    private final UserImportResultDto result;

    // 使用缓存避免在同一次导入中重复查询相同的部门
    private final Map<String, Department> departmentCache = new ConcurrentHashMap<>();

    @Override
    public void invoke(DepartmentImportDto data, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex() + 1;
        try {
            if (!StringUtils.hasText(data.getLevelOneDepartment())) {
                // 如果第一级部门为空，则跳过此行
                return;
            }

            // 依次处理各级部门，构建层级关系
            Department level1 = findOrCreateDepartment(data.getLevelOneDepartment(), null);
            Department level2 = findOrCreateDepartment(data.getLevelTwoDepartment(), level1);
            Department level3 = findOrCreateDepartment(data.getLevelThreeDepartment(), level2);
            findOrCreateDepartment(data.getLevelFourDepartment(), level3); // 最后一级

            result.setSuccessCount(result.getSuccessCount() + 1);
        } catch (Exception e) {
            log.error("处理部门导入第 {} 行数据失败: {}", rowIndex, data, e);
            result.setFailureCount(result.getFailureCount() + 1);
            result.addFailureDetail("第 " + rowIndex + " 行处理失败: " + e.getMessage());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("部门Excel导入完成。成功: {}, 失败: {}", result.getSuccessCount(), result.getFailureCount());
    }

    /**
     * 根据父部门和名称查找或创建新部门
     */
    private Department findOrCreateDepartment(String name, Department parent) {
        if (!StringUtils.hasText(name)) {
            // 如果当前级别的名称为空，则层级在此结束，返回其父级
            return parent;
        }

        // 缓存键必须唯一，结合父ID和当前名称
        String cacheKey = (parent == null ? "null" : parent.getId()) + "_" + name;

        return departmentCache.computeIfAbsent(cacheKey, k -> {
            QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", name);
            queryWrapper.eq(parent != null, "parent_id", parent != null ? parent.getId() : null);
            queryWrapper.isNull(parent == null, "parent_id");

            Department existingDept = departmentMapper.selectOne(queryWrapper);
            if (existingDept != null) {
                return existingDept;
            } else {
                Department newDept = new Department(name, parent == null ? 1 : parent.getLevel() + 1, parent);
                departmentMapper.insert(newDept);
                return newDept;
            }
        });
    }
}