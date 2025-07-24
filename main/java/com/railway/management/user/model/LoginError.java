package com.railway.management.user.model;

import lombok.Getter;

/**
 * 登录错误信息枚举
 */
@Getter // 使用Lombok自动生成getter方法
public enum LoginError {

    ACCOUNT_NOT_FOUND(1001, "账号不存在"),
    INVALID_CREDENTIALS(1002, "账号或密码错误"),
    ACCOUNT_LOCKED(1003, "账号已锁定"),
    ACCOUNT_DISABLED(1004, "账号已禁用"),
    TOO_MANY_ATTEMPTS(1005, "尝试次数过多，请稍后再试"),
    UNKNOWN_ERROR(9999, "未知错误"),
    TOKEN_EXPIRED(1006, "登录已过期，请重新登录"); // Added token expiration error

    private final int code;
    private final String message;

    LoginError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // 可以在这里添加一些辅助方法，例如根据错误代码获取枚举值
    // public static LoginError fromCode(int code) { ... }

}