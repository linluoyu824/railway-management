package com.railway.managementsystem.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.railway.managementsystem.department.mapper.DepartmentMapper;
import com.railway.managementsystem.department.model.Department;
import com.railway.managementsystem.position.mapper.PositionMapper;
import com.railway.managementsystem.position.model.Position;
import com.railway.managementsystem.user.dto.UserImportDto;
import com.railway.managementsystem.user.dto.UserImportResultDto;
import com.railway.managementsystem.user.mapper.UserMapper;
import com.railway.managementsystem.user.model.DriverLicenseType;
import com.railway.managementsystem.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EasyExcel监听器，用于逐行处理用户导入数据。
 * 此版本已适配MyBatis-Plus。
 */
@RequiredArgsConstructor
@Slf4j
public class UserImportListener implements ReadListener<UserImportDto> {

    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final PositionMapper positionMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserImportResultDto result;

    // 使用缓存避免在同一次导入中重复查询相同的部门和职位
    private final Map<String, Department> departmentCache = new ConcurrentHashMap<>();
    private final Map<String, Position> positionCache = new ConcurrentHashMap<>();

    private static final int BATCH_SIZE = 100;
    private final List<User> cachedDataList = new ArrayList<>(BATCH_SIZE);

    @Override
    public void invoke(UserImportDto data, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex() + 1;
        try {
            if (!StringUtils.hasText(data.getEmployeeId())) {
                throw new IllegalArgumentException("员工工号不能为空。");
            }

            // 检查员工工号是否已存在
            if (userMapper.selectCount(new QueryWrapper<User>().eq("employee_id", data.getEmployeeId())) > 0) {
                throw new IllegalArgumentException(String.format("员工工号 '%s' 已存在。", data.getEmployeeId()));
            }

            // 1. 动态处理部门层级
            Department section = findOrCreateDepartment(data.getSection(), null);
            Department workshop = findOrCreateDepartment(data.getWorkshop(), section);
            Department team = findOrCreateDepartment(data.getTeam(), workshop);
            Department finalDepartment = findOrCreateDepartment(data.getGuidanceGroup(), team);

            // 2. 动态处理职位 (职位属于最终的部门)
            Position position = findOrCreatePosition(data.getJobTitle(), finalDepartment);

            // 3. 创建用户实体
            User user = new User();
            user.setEmployeeId(data.getEmployeeId());
            user.setFullName(data.getFullName());
            user.setMobilePhone(data.getMobilePhone());
            user.setPinyinCode(data.getPinyinCode());

            // 默认使用工号作为用户名
            user.setUsername(data.getEmployeeId());
            // 设置默认密码，应提示用户首次登录后修改
            if (passwordEncoder != null) {
                user.setPassword(passwordEncoder.encode("Default123456"));
            }

            user.setDepartment(finalDepartment);
            user.setPosition(position);
            user.setJobLevel(1); // 设置默认职级

            if (StringUtils.hasText(data.getDriverLicenseType())) {
                try {
                    user.setDriverLicenseType(DriverLicenseType.valueOf(data.getDriverLicenseType().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("行 {}: 无效的驾驶证类型 '{}'，将被忽略。", rowIndex, data.getDriverLicenseType());
                }
            }

            cachedDataList.add(user);
            if (cachedDataList.size() >= BATCH_SIZE) {
                saveData();
            }
            result.addSuccess();
        } catch (Exception e) {
            log.error("处理第 {} 行数据失败: {}", rowIndex, data, e);
            result.addFailure(rowIndex, e.getMessage());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 保存最后一批数据
        if (!cachedDataList.isEmpty()) {
            saveData();
        }
        log.info("Excel导入完成。成功: {}, 失败: {}", result.getSuccessCount(), result.getFailureCount());
    }

    private void saveData() {
        // 遍历并插入，在外层Service中应开启事务以保证原子性
        for (User user : cachedDataList) {
            userMapper.insert(user);
        }
        cachedDataList.clear();
    }

    private Department findOrCreateDepartment(String name, Department parent) {
        if (!StringUtils.hasText(name)) return parent;
        String cacheKey = (parent == null ? "null" : parent.getId()) + "_" + name;

        return departmentCache.computeIfAbsent(cacheKey, k -> {
            QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", name);
            if (parent == null) {
                queryWrapper.isNull("parent_id");
            } else {
                queryWrapper.eq("parent_id", parent.getId());
            }
            
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

    private Position findOrCreatePosition(String name, Department department) {
        if (!StringUtils.hasText(name) || department == null) return null;
        String cacheKey = department.getId() + "_" + name;
        return positionCache.computeIfAbsent(cacheKey, k ->
                positionMapper.selectOne(new QueryWrapper<Position>().eq("name", name).eq("department_id", department.getId()))
                        .orElseGet(() -> {
                            Position newPos = new Position();
                            newPos.setName(name);
                            newPos.setDepartment(department);
                            positionMapper.insert(newPos);
                            return newPos;
                        })
        );
    }
}