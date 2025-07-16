package com.railway.managementsystem.user.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.managementsystem.department.mapper.DepartmentMapper;
import com.railway.managementsystem.position.mapper.PositionMapper;
import com.railway.managementsystem.user.dto.UserDto;
import com.railway.managementsystem.user.dto.UserImportDto;
import com.railway.managementsystem.user.dto.UserImportResultDto;
import com.railway.managementsystem.user.dto.UserRegistrationDto;
import com.railway.managementsystem.user.exception.UserAlreadyExistsException;
import com.railway.managementsystem.user.model.User;
import com.railway.managementsystem.user.service.UserService;
import com.railway.managementsystem.user.mapper.UserMapper;
import com.railway.managementsystem.utils.UserImportListener;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class UserServiceImpl implements UserService{

    private final DelegatingPasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final PositionMapper positionMapper;


    public UserServiceImpl(DelegatingPasswordEncoder passwordEncoder, UserMapper userMapper, DepartmentMapper departmentMapper, PositionMapper positionMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
        this.positionMapper = positionMapper;
    }

    @Override
    public User registerUser(UserRegistrationDto registrationDto) {
        // 检查用户名是否已存在
        if (userMapper.selectOne(new QueryWrapper<User>().eq("username", registrationDto.getUsername())) != null) {
            throw new UserAlreadyExistsException("用户名 " + registrationDto.getUsername() + " 已被注册");
        }
        // 检查员工号是否已存在

        if (userMapper.selectOne(new QueryWrapper<User>().eq("employee_id", registrationDto.getEmployeeId())) != null) {
            throw new UserAlreadyExistsException("员工号 " + registrationDto.getEmployeeId() + " 已被注册");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        // Check if passwordEncoder is null before using it
        if (passwordEncoder != null) {
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword())); // 加密密码
        }
        user.setFullName(registrationDto.getFullName());
        user.setEmployeeId(registrationDto.getEmployeeId());

        userMapper.insert(user);
        return user;
    }

    @Override
    public User updateUserPosition(Long userId, Long newPositionId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            //  TODO: 在这里添加职位变更记录到 PositionChange 表
            user.getPosition().setId(newPositionId);
            userMapper.updateById(user); // 注意：这里只会更新user表，position_id会更新。如果User实体中加载了Position对象，这个操作是正确的。
            return user;
        } else {
            return null; // 或者抛出异常，例如 UserNotFoundException
        }
    }

    @Override
    public List<User> getEmployeesByLevel(Integer jobLevel) {
        if (jobLevel == 2) { // 只有工班长（等级2）才能查看员工列表
            // 可以根据实际需求添加更复杂的权限判断逻辑
            return userMapper.selectList(new QueryWrapper<User>().eq("job_level", 1)); // 获取一般职工（等级1）的列表
        }
        return null;  // 或者返回一个空列表，或抛出异常，表示无权访问
    }

    @Override
    public IPage<UserDto> getManagedEmployees(Page<UserDto> page) {
        return userMapper.selectUserPage(page);
    }

    @Override
    public UserImportResultDto importUsersFromExcel(InputStream inputStream) {
        // Keep this method as it was, or refactor to use MyBatis-Plus if needed in the future.
        // It's a separate batch operation, so doesn't strictly need to use the same pagination approach.
        // The listener will need to be updated to use Mappers instead of Repositories.
        UserImportResultDto result = new UserImportResultDto();
        UserImportListener listener = new UserImportListener(userMapper, departmentMapper, positionMapper, passwordEncoder, result);
        EasyExcel.read(inputStream, UserImportDto.class, listener).sheet().doRead();
        return result;
    }
}