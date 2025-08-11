package com.railway.management.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.common.department.mapper.DepartmentMapper;
import com.railway.management.common.department.model.Department;
import com.railway.management.permission.position.mapper.PositionMapper;
import com.railway.management.permission.position.model.Position;
import com.railway.management.common.user.dto.UserImportDto;
import com.railway.management.common.user.mapper.UserMapper;
import com.railway.management.common.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class UserImportListener extends AnalysisEventListener<UserImportDto> {

    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final PositionMapper positionMapper;
    private final PasswordEncoder passwordEncoder;
    private final ExcelImportResult<UserImportDto> result;

    // Caches to avoid repeated DB queries during a single import
    private final Map<String, Department> departmentCache = new HashMap<>();
    private final Map<String, Position> positionCache = new HashMap<>();

    public UserImportListener(UserMapper userMapper, DepartmentMapper departmentMapper, PositionMapper positionMapper, PasswordEncoder passwordEncoder, ExcelImportResult<UserImportDto> result) {
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
        this.positionMapper = positionMapper;
        this.passwordEncoder = passwordEncoder;
        this.result = result;
    }

    @Override
    public void invoke(UserImportDto data, AnalysisContext context) {
        String employeeId = data.getEmployeeId();
        if (!StringUtils.hasText(employeeId)) {
            result.addFailure(data, "第 " + (context.readRowHolder().getRowIndex() + 1) + " 行：工号为空");
            return;
        }

        // 1. Check if user already exists
        if (userMapper.selectCount(new QueryWrapper<User>().eq("employee_id", employeeId)) > 0) {
            result.addFailure(data, "工号 " + employeeId + " 已存在");
            return;
        }

        try {
            // 2. Handle department hierarchy
            Department finalDepartment = getOrCreateDepartmentHierarchy(data);
            if (finalDepartment == null) {
                result.addFailure(data, "未能确定部门信息");
                return;
            }

            // 3. Handle position
            Position position = getOrCreatePosition(data.getJobTitle(), finalDepartment.getId());

            // 4. Create new User entity
            User user = new User();
            user.setEmployeeId(employeeId);
            user.setFullName(data.getFullName());
            user.setMobilePhone(data.getMobilePhone());
            user.setDepartmentId(finalDepartment.getId());
            user.setPositionId(position != null ? position.getId() : null);

            // Generate default username and password
            user.setUsername(data.getMobilePhone()); // Use mobilePhone as username
            user.setPassword(passwordEncoder.encode("123456")); // Default password, should be configurable
            user.generatePinyinCode(); // Generate pinyin from fullName

            userMapper.insert(user);

            result.incrementSuccessCount(1);
        } catch (Exception e) {
            log.error("导入用户失败，工号: {}", employeeId, e);
            result.addFailure(data, "导入失败: " + e.getMessage());
        }
    }

    private Department getOrCreateDepartmentHierarchy(UserImportDto data) {
        List<String> departmentNames = Arrays.asList(
                data.getSection(),
                data.getWorkshop(),
                data.getTeam(),
                data.getGuidanceGroup()
        );

        Long parentId = null;
        Department currentDepartment = null;
        int level = 1;

        for (String deptName : departmentNames) {
            if (!StringUtils.hasText(deptName)) {
                break;
            }

            String cacheKey = (parentId == null ? "null" : parentId) + ":" + deptName;
            currentDepartment = departmentCache.get(cacheKey);

            if (currentDepartment == null) {
                QueryWrapper<Department> queryWrapper = new QueryWrapper<Department>().eq("name", deptName);
                queryWrapper.eq(parentId != null, "parent_id", parentId);
                queryWrapper.isNull(parentId == null, "parent_id");
                currentDepartment = departmentMapper.selectOne(queryWrapper);

                if (currentDepartment == null) {
                    currentDepartment = new Department(deptName, level, null);
                    currentDepartment.setParentId(parentId);
                    departmentMapper.insert(currentDepartment);
                }
                departmentCache.put(cacheKey, currentDepartment);
            }
            parentId = currentDepartment.getId();
            level++;
        }
        return currentDepartment;
    }

    private Position getOrCreatePosition(String positionName, Long departmentId) {
        if (!StringUtils.hasText(positionName)) return null;
        String cacheKey = departmentId + ":" + positionName;
        Position position = positionCache.get(cacheKey);
        if (position == null) {
            position = positionMapper.selectOne(new QueryWrapper<Position>().eq("name", positionName).eq("department_id", departmentId));
            if (position == null) {
                position = new Position();
                position.setName(positionName);
                position.setDepartmentId(departmentId);
                positionMapper.insert(position);
            }
            positionCache.put(cacheKey, position);
        }
        return position;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("所有数据解析完成！成功: {}, 失败: {}", result.getSuccessCount(), result.getFailedRows().size());
    }
}