package com.railway.management.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j; 
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 用于自动填充创建时间、更新时间、创建人、更新人等字段。
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("start insert fill ....");
        // 填充创建时间和更新时间
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);

        // 填充创建人和更新人
        String currentUsername = getCurrentUsername();
        this.strictInsertFill(metaObject, "createdBy", () -> currentUsername, String.class);
        this.strictInsertFill(metaObject, "updatedBy", () -> currentUsername, String.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("start update fill ....");
        // 仅填充更新时间
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
        // 仅填充更新人
        this.strictUpdateFill(metaObject, "updatedBy", () -> getCurrentUsername(), String.class);
    }

    /**
     * 获取当前登录的用户名，如果获取不到则返回 "system"
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "system"; // 对于系统自动任务或未登录操作，使用默认值
    }
}