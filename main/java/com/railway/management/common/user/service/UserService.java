package com.railway.management.common.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.common.user.dto.UserDto;
import com.railway.management.common.user.dto.UserImportDto;
import com.railway.management.common.user.dto.UserRegistrationDto;
import com.railway.management.common.user.model.User;
import java.io.InputStream;
import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 注册新用户
     *
     * @param registrationDto 用户注册信息
     * @return 创建后的用户实体
     * @throws com.railway.management.common.user.exception.UserAlreadyExistsException 如果用户名或员工号已存在
     */
    User registerUser(UserRegistrationDto registrationDto);

    /**
     * 更新用户职位
     *
     * @param userId        用户ID
     * @param newPositionId 新职位ID
     * @return 更新后的用户实体
     */
    User updateUserPosition(Long userId, Long newPositionId);

    /**
     * 根据职级获取员工列表
     * @param jobLevel 职级
     * @return 用户实体列表
     */
    List<User> getEmployeesByLevel(Integer jobLevel);

    /**
     * 分页获取其管理的职工列表
     *
     * @param page 分页信息
     * @return 分页后的职工列表 (IPage<UserDto>)
     */
    IPage<UserDto> getManagedEmployees(Page<UserDto> page);

    /**
     * 从Excel批量导入用户
     * @param inputStream Excel文件输入流
     * @return 导入结果报告
     */
    ExcelImportResult<UserImportDto> importUsersFromExcel(InputStream inputStream);
    
    /**
     * 验证用户登录凭证
     *
     * @param username 用户名
     * @param password 密码
     * @return 如果凭证有效则返回 true, 否则返回 false
     */
    boolean validateCredentials(String username, String password);

    /**
     * 为已验证的用户生成登录令牌
     * @param username 用户名
     * @return 登录令牌 (Token)
     */
    String generateLoginToken(String username);
}
