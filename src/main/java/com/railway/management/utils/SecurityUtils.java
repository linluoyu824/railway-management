package com.railway.management.utils;

import com.railway.management.common.user.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    /**
     * 获取当前登录的用户信息
     * <p>
     * 如果安全上下文中没有认证信息或用户主体不是 User 类的实例，
     * 将抛出 IllegalStateException。
     *
     * @return 当前登录的 User 对象，绝不为 null
     * @throws IllegalStateException 如果无法获取当前用户信息
     */
    public static User getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(User.class::isInstance)
                .map(User.class::cast)
                .orElseThrow(() -> new IllegalStateException("无法获取当前用户信息，请确认用户已登录。"));
    }
}