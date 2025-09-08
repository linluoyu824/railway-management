package com.railway.management.equipment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.railway.management.equipment.model.Tool;

import java.util.List;

public interface ToolService extends IService<Tool> {

    /**
     * @deprecated 请使用 IService 提供的 listByIds(ids) 方法。此方法为保持向后兼容而提供。
     */
    @Deprecated
    default List<Tool> getByIds(List<Long> ids) {
        return listByIds(ids);
    }
}