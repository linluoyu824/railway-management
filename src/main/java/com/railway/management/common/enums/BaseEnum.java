package com.railway.management.common.enums;

/**
 * 通用枚举接口
 * @param <V> 数据库存储值的类型
 */
public interface BaseEnum<V> {
    /**
     * 获取存储到数据库的值
     */
    V getValue();

    /**
     * 获取用于前端展示的描述
     */
    String getDescription();
}