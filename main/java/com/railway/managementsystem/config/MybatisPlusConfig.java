package com.railway.managementsystem.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.railway.managementsystem.user.mapper.UserMapper;
import com.railway.managementsystem.user.model.User;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class MybatisPlusConfig {

    private final UserMapper userMapper;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 添加多租户插件
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            private final List<String> IGNORED_TABLES = Arrays.asList("permissions", "departments");

            @Override
            public Expression getTenantId() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                    return null; // 不返回租户ID，即不进行过滤
                }

                String username = authentication.getName();
                User user = userMapper.selectByUsername(username);

                // 如果用户不存在，或者用户没有部门ID（如超级管理员），则不进行过滤
                if (user == null || user.getDepartmentId() == null) {
                    return null;
                }
                return new LongValue(user.getDepartmentId());
            }

            @Override
            public String getTenantIdColumn() {
                return "department_id"; // 租户ID的数据库列名
            }

            @Override
            public boolean ignoreTable(String tableName) {
                return IGNORED_TABLES.contains(tableName.toLowerCase());
            }
        }));

        // 2. 添加分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());

        return interceptor;
    }
}