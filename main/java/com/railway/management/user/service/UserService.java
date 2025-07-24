package com.railway.management.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.user.dto.UserDto;
import com.railway.management.user.dto.UserImportResultDto;
import com.railway.management.user.dto.UserRegistrationDto;
import com.railway.management.user.model.User;
import java.io.InputStream;
import java.util.List;

public interface UserService {
    /**
     * 注册用户
     *
     * @param registrationDto 注册信息
     * @return 已注册的用户
     */
    User registerUser(UserRegistrationDto registrationDto);

    /**
     * 职位变更
     *
     * @param userId        用户ID
     * @param newPositionId 新职位ID  
     * @return 更新后的用户
     */
    User updateUserPosition(Long userId, Long newPositionId);

    /**
     * @param jobLevel
     * @return
     */
    List<User> getEmployeesByLevel(Integer jobLevel);

    /**
     * 获取职工列表（分页）
     *
     * @param page 分页信息
     * @return 分页后的职工列表 (IPage<UserDto>)
     */
    IPage<UserDto> getManagedEmployees(Page<UserDto> page);

    /**
     * excel批量导入
     * @param inputStream
     * @return
     */
    UserImportResultDto importUsersFromExcel(InputStream inputStream);
    
    /**
     * 验证登录凭证是否正确
     *
     * @param username 用户名
     * @param password 密码
     * @return 如果凭证有效则返回 true, 否则返回 false
     */
    boolean validateCredentials(String username, String password);

    // Method to generate a token for a validated user
    String generateLoginToken(String username);
}
