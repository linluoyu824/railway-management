package com.railway.management.equipment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.railway.management.equipment.model.Tool;
import com.railway.management.equipment.dto.ToolCreateNfcDto;
import com.railway.management.equipment.model.ToolLog;

import java.util.List;

public interface ToolService extends IService<Tool> {

    /**
     * @deprecated 请使用 IService 提供的 listByIds(ids) 方法。此方法为保持向后兼容而提供。
     */
    @Deprecated
    default List<Tool> getByIds(List<Long> ids) {
        return listByIds(ids);
    }

    /**
     * 处理工具的NFC扫描事件 (借出/归还)
     * @param nfcId 工具的NFC ID
     * @return 生成的工具操作日志
     */
    ToolLog processNfcScan(String nfcId);

    /**
     * 通过NFC扫描创建新工具
     * @param dto 包含NFC ID和工具信息的DTO
     * @return 创建的工具实体
     */
    Tool createFromNfc(ToolCreateNfcDto dto);
}