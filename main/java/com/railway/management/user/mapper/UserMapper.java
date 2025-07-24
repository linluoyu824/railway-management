package com.railway.management.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.user.dto.UserDto;
import com.railway.management.user.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户，包含密码
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 分页查询用户 DTO
     */
    IPage<UserDto> selectUserPage(Page<UserDto> page);
}