package com.railway.managementsystem.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.railway.managementsystem.user.dto.UserDto;
import com.railway.managementsystem.user.model.User;
import org.apache.ibatis.annotations.Mapper;

public interface UserMapper extends BaseMapper<User> {

    /**
     * Custom query for paginated user retrieval.
     * You can customize the SQL as needed, including joins, filtering, etc.
     *
     * @param page MyBatis-Plus pagination object
     * @return Paginated result of UserDto
     */
    IPage<UserDto> selectUserPage(IPage<?> page);  // Use a wildcard IPage for input

    // You can add more custom queries as needed
}