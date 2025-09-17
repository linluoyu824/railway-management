package com.railway.management.equipment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.railway.management.equipment.dto.*;
import com.railway.management.equipment.model.AccessControl;
import com.railway.management.equipment.model.AccessControlAction;
import com.railway.management.equipment.model.AccessControlLog;
import com.railway.management.equipment.model.AccessPermissionRequest;

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
     * 记录由用户NFC刷卡触发的门禁事件 (如进门、出门)
     * @param accessControlId 门禁ID
     * @param action 门禁操作类型 (ENTER/EXIT)
     * @return 记录的门禁日志实体
     */
    AccessControlLog recordAccess(Long accessControlId, AccessControlAction action);

    /**
     * 记录由门禁设备本身触发的物理事件 (如门磁开关、报警)
     * @param logDto 包含事件详情的DTO
     * @return 记录的门禁日志实体
     */
    AccessControlLog recordAction(AccessControlLogCreateDto logDto);
    /**
     * 根据门禁设备ID分页查询其操作记录
     * @param page 分页对象
     * @param accessControlId 门禁设备ID
     * @return 门禁操作记录分页数据
     */
    IPage<AccessControlLog> getLogsByAccessControlId(IPage<AccessControlLog> page, Long accessControlId);

    /**
     * 通过NFC扫描创建新门禁
     * @param dto 包含NFC ID和门禁信息的DTO
     * @return 创建的门禁实体
     */
    AccessControl createFromNfc(AccessControlCreateNfcDto dto);

    /**
     * 申请门禁权限
     * @param requestDto dto 工具ID与人员ID集合
     * @return 权限申请实体
     */
    AccessPermissionRequest applyForPermission(RequestAccessDto requestDto);
    /**
     * 审批门禁权限申请
     * @param permissionRequestId 权限申请ID
     * @param approveDto 审批信息DTO
     * @return 更新后的权限申请实体
     */
    AccessPermissionRequest handlePermissionRequestApproval(Long permissionRequestId, ApprovePermissionRequestDto approveDto);
}