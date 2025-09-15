package com.railway.management.equipment.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.railway.management.common.department.service.DepartmentService;
import com.railway.management.common.user.model.User;
import com.railway.management.equipment.dto.*;
import com.railway.management.equipment.mapper.AccessControlLogMapper;
import com.railway.management.equipment.mapper.AccessControlMapper;
import com.railway.management.equipment.mapper.AccessPermissionRequestMapper;
import com.railway.management.equipment.model.*;
import com.railway.management.equipment.service.AccessControlService;
import com.railway.management.equipment.service.ToolService;
import com.railway.management.utils.SecurityUtils;
import com.railway.management.workorder.mapper.WorkAttendanceMapper;
import com.railway.management.workorder.mapper.WorkOrderMapper;
import com.railway.management.workorder.model.WorkAttendance;
import com.railway.management.workorder.model.WorkAttendanceType;
import com.railway.management.workorder.model.WorkOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessControlServiceImpl extends ServiceImpl<AccessControlMapper, AccessControl> implements AccessControlService {

    private final AccessControlLogMapper accessControlLogMapper;
    private final AccessControlMapper accessControlMapper;
    private final DepartmentService departmentService;
    private final WorkOrderMapper workOrderMapper;
    private final WorkAttendanceMapper workAttendanceMapper;
    private final ToolService toolService;
    private final AccessPermissionRequestMapper accessPermissionRequestMapper;


    @Override
    public IPage<AccessControl> listAccessControls(IPage<AccessControl> page, String departmentPath) {
        QueryWrapper<AccessControl> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("department_path", departmentPath);
        queryWrapper.orderByDesc("created_at");
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional
    public AccessControl createAccessControl(AccessControlCreateDto createDto) {
        AccessControl accessControl = new AccessControl();
        accessControl.setName(createDto.getName());
        accessControl.setLocation(createDto.getLocation());
        accessControl.setIpAddress(createDto.getIpAddress());
        accessControl.setDepartmentId(createDto.getDepartmentId());
        accessControl.setStatus(AccessControlStatus.ONLINE); // 默认状态

        // 构建并设置部门路径
        String departmentPath = departmentService.buildDepartmentPath(createDto.getDepartmentId());
        if (departmentPath == null) {
            throw new IllegalArgumentException("无效的部门ID: " + createDto.getDepartmentId());
        }
        accessControl.setDepartmentPath(departmentPath);

        this.save(accessControl);
        return accessControl;
    }

    @Override
    @Transactional
    public AccessControl updateAccessControl(AccessControlUpdateDto updateDto) {
        AccessControl existing = this.getById(updateDto.getId());
        if (existing == null) {
            throw new IllegalArgumentException("门禁设备不存在，ID: " + updateDto.getId());
        }
        existing.setName(updateDto.getName());
        existing.setLocation(updateDto.getLocation());
        existing.setIpAddress(updateDto.getIpAddress());
        if (updateDto.getStatus() != null) {
            existing.setStatus(updateDto.getStatus());
        }
        this.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deleteAccessControl(Long id) {
        if (!this.removeById(id)) {
            throw new IllegalArgumentException("删除失败，门禁设备不存在，ID: " + id);
        }
    }

    @Override
    @Transactional
    public AccessControlLog recordAction(AccessControlLogCreateDto logDto) {
        // 校验门禁设备是否存在
        Assert.notNull(this.getById(logDto.getAccessControlId()),
                "门禁设备不存在，ID: " + logDto.getAccessControlId());

        AccessControlLog log = new AccessControlLog();
        log.setAccessControlId(logDto.getAccessControlId());
        log.setUserId(logDto.getUserId());
        log.setUsername(logDto.getUsername());
        log.setAction(logDto.getAction());
        log.setSuccess(logDto.isSuccess());
        log.setRemark(logDto.getRemark());
        log.setTimestamp(LocalDateTime.now());

        accessControlLogMapper.insert(log);
        return log;
    }

    @Override
    @Transactional
    public AccessControlLog recordAccess(Long accessControlId, AccessControlAction action) {
        User currentUser = SecurityUtils.getCurrentUser(); // 安全地获取当前用户

        // 1. 校验门禁是否存在
        AccessControl accessControl = accessControlMapper.selectById(accessControlId);
        Assert.notNull(accessControl, "门禁不存在: " + accessControlId);

        AccessControlLog log = new AccessControlLog();
        log.setAccessControlId(accessControlId);
        log.setUserId(currentUser.getId());
        log.setUsername(currentUser.getUsername());
        log.setAction(action);
        log.setSuccess(true); // NFC扫描成功即为成功
        log.setTimestamp(LocalDateTime.now());

        accessControlLogMapper.insert(log);
        return log;
    }

    @Override
    public IPage<AccessControlLog> getLogsByAccessControlId(IPage<AccessControlLog> page, Long accessControlId) {
        QueryWrapper<AccessControlLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("access_control_id", accessControlId);
        queryWrapper.orderByDesc("timestamp");
        return accessControlLogMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional
    public AccessControl createFromNfc(AccessControlCreateNfcDto dto) {
        Assert.notBlank(dto.getNfcId(), "NFC ID不能为空");
        Assert.notBlank(dto.getName(), "新门禁名称不能为空");
        Assert.notNull(dto.getDepartmentId(), "所属部门不能为空");

        // 1. Check for duplicates
        Assert.isFalse(this.exists(new QueryWrapper<AccessControl>().eq("nfc_id", dto.getNfcId())),
                "NFC ID {} 已被注册", dto.getNfcId());

        // 2. Create and save
        AccessControl accessControl = new AccessControl();
        accessControl.setNfcId(dto.getNfcId());
        accessControl.setName(dto.getName());
        accessControl.setLocation(dto.getLocation());
        accessControl.setDepartmentId(dto.getDepartmentId());
        accessControl.setStatus(AccessControlStatus.NORMAL); // Default status
        this.save(accessControl);
        log.info("通过NFC成功注册新门禁: {}", dto.getName());
        return accessControl;
    }

    @Override
    @Transactional
    public AccessPermissionRequest applyForPermission(RequestAccessDto requestDto) {
        // 1. 校验工单和门禁是否存在
        WorkOrder workOrder = workOrderMapper.selectById(requestDto.getWorkOrderId());
        Assert.notNull(workOrder, "工单不存在，ID: {}", requestDto.getWorkOrderId());
        Assert.notNull(this.getById(requestDto.getAccessControlId()), "门禁不存在，ID: {}", requestDto.getAccessControlId());

        // 2. 检查出工人员是否齐全
        String assigneeIdsStr = workOrder.getAssigneeIds();
        if (StringUtils.hasText(assigneeIdsStr)) {
            List<Long> assigneeIds = Arrays.stream(assigneeIdsStr.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            if (!assigneeIds.isEmpty()) {
                List<WorkAttendance> attendances = workAttendanceMapper.selectList(new QueryWrapper<WorkAttendance>()
                        .eq("work_order_id", workOrder.getId())
                        .eq("attendance_type", WorkAttendanceType.CLOCK_IN)
                        .in("user_id", assigneeIds));
                long distinctClockedInCount = attendances.stream().map(WorkAttendance::getUserId).distinct().count();
                Assert.isTrue(distinctClockedInCount == assigneeIds.size(), "人员未全部到场，无法申请门禁权限");
            }
        }

        // 3. 检查所需工具是否齐全 (已领用)
        String toolsStr = workOrder.getTools();
        if (StringUtils.hasText(toolsStr)) {
            List<String> toolNames = Arrays.stream(toolsStr.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());

            if (!toolNames.isEmpty()) {
                List<Tool> requiredTools = toolService.list(new QueryWrapper<Tool>().in("name", toolNames));
                Assert.isTrue(requiredTools.size() == toolNames.size(), "工单所需的部分工具在系统中不存在，无法申请门禁权限");
                for (Tool tool : requiredTools) {
                    Assert.isTrue(tool.getStatus() == ToolStatus.IN_USE, "工具 '{}' 未领用，无法申请门禁权限", tool.getName());
                }
            }
        }

        // 4. 创建权限申请记录
        User currentUser = SecurityUtils.getCurrentUser();
        AccessPermissionRequest permissionRequest = new AccessPermissionRequest();
        permissionRequest.setWorkOrderId(workOrder.getId());
        permissionRequest.setAccessControlId(requestDto.getAccessControlId());
        permissionRequest.setRequesterId(currentUser.getId());
        permissionRequest.setRequesterName(currentUser.getUsername());
        permissionRequest.setApproverId(workOrder.getCreatorId()); // 指派给工单创建者审批
        permissionRequest.setStatus(AccessPermissionRequestStatus.PENDING);
        permissionRequest.setCreatedAt(LocalDateTime.now());
        permissionRequest.setUpdatedAt(LocalDateTime.now());

        accessPermissionRequestMapper.insert(permissionRequest);
        log.info("用户 {} 为工单 {} 申请门禁 {} 权限成功，等待审批。", currentUser.getUsername(), workOrder.getId(), requestDto.getAccessControlId());

        return permissionRequest;
    }
}