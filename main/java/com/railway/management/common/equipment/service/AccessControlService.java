package com.railway.management.common.equipment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.railway.management.common.equipment.dto.AccessControlCreateDto;
import com.railway.management.common.equipment.dto.AccessControlLogCreateDto;
import com.railway.management.common.equipment.dto.AccessControlUpdateDto;
import com.railway.management.common.equipment.model.AccessControl;
import com.railway.management.common.equipment.model.AccessControlLog;

/**
 * 门禁设备服务接口
 */
public interface AccessControlService extends IService<AccessControl> {
    /**
     * 分页查询门禁设备列表
     * @param page 分页对象
     * @param departmentPath 部门路径，用于数据隔离
     * @return 门禁设备分页数据
     */
    IPage<AccessControl> listAccessControls(IPage<AccessControl> page, String departmentPath);
    /**
     * 创建新的门禁设备
     * @param createDto 创建门禁设备的数据传输对象
     * @return 创建后的门禁设备实体
     */
    AccessControl createAccessControl(AccessControlCreateDto createDto);
    /**
     * 更新门禁设备信息
     * @param updateDto 更新门禁设备的数据传输对象
     * @return 更新后的门禁设备实体
     */
    AccessControl updateAccessControl(AccessControlUpdateDto updateDto);
    /**
     * 根据ID删除门禁设备
     * @param id 门禁设备ID
     */
    void deleteAccessControl(Long id);
    /**
     * 记录门禁操作（如开门、关门）
     * @param logDto 门禁操作日志的数据传输对象
     * @return 创建的门禁操作日志实体
     */
    AccessControlLog recordAction(AccessControlLogCreateDto logDto);
    /**
     * 根据门禁设备ID分页查询其操作记录
     * @param page 分页对象
     * @param accessControlId 门禁设备ID
     * @return 门禁操作记录分页数据
     */
    IPage<AccessControlLog> getLogsByAccessControlId(IPage<AccessControlLog> page, Long accessControlId);
}