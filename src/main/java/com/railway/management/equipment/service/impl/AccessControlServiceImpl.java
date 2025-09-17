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
import com.railway.management.workorder.mapper.WorkOrderAssigneeMapper;
import com.railway.management.workorder.mapper.WorkOrderMapper;
import com.railway.management.workorder.model.WorkOrder;
import com.railway.management.workorder.model.WorkOrderAssignee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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
    private final WorkOrderAssigneeMapper workOrderAssigneeMapper;


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

        // 2. 检查出工人员是否齐全 (优化后)
        // 2.1 从关联表获取所有需要出工的人员ID
        List<Long> assigneeIds = workOrderAssigneeMapper.selectList(
                        new QueryWrapper<WorkOrderAssignee>().eq("work_order_id", workOrder.getId())
                ).stream()
                .map(WorkOrderAssignee::getUserId)
                .collect(Collectors.toList());

        if (!assigneeIds.isEmpty()) {
            // 2.2 直接调用Mapper方法在数据库中进行计数，性能更高
            Long clockedInCount = workAttendanceMapper.countDistinctClockedInUsers(workOrder.getId(), assigneeIds);
            Assert.isTrue(clockedInCount.equals((long) assigneeIds.size()), "人员未全部到场，无法申请门禁权限");
        }

        // 3. 检查所需工具是否齐全 (逻辑不变，但建议同样使用关联表优化)
        String toolsStr = workOrder.getTools();
        if (StringUtils.hasText(toolsStr)) {
            // ... (此部分逻辑建议在`tools`字段也被重构为关联表后进行相应优化)
        }

        // 4. 创建权限申请记录 (逻辑不变)
        User currentUser = SecurityUtils.getCurrentUser();
        AccessPermissionRequest permissionRequest = new AccessPermissionRequest();
        permissionRequest.setWorkOrderId(workOrder.getId());
        permissionRequest.setAccessControlId(requestDto.getAccessControlId());
        permissionRequest.setRequesterId(currentUser.getId());
        permissionRequest.setRequesterName(currentUser.getUsername());
        permissionRequest.setApproverId(workOrder.getCreatorId());
        permissionRequest.setStatus(AccessPermissionRequestStatus.PENDING);
        permissionRequest.setCreatedAt(LocalDateTime.now());
        permissionRequest.setUpdatedAt(LocalDateTime.now());

        accessPermissionRequestMapper.insert(permissionRequest);
        log.info("用户 {} 为工单 {} 申请门禁 {} 权限成功，等待审批。", currentUser.getUsername(), workOrder.getId(), requestDto.getAccessControlId());

        return permissionRequest;
    }


    @Override
    @Transactional
    public AccessPermissionRequest handlePermissionRequestApproval(Long permissionRequestId, ApprovePermissionRequestDto approveDto) {
        // 1. 校验权限申请是否存在
        AccessPermissionRequest permissionRequest = accessPermissionRequestMapper.selectById(permissionRequestId);
        Assert.notNull(permissionRequest, "权限申请不存在，ID: {}", permissionRequestId);

        // 2. 校验当前用户是否为指定的审批人
        User currentUser = SecurityUtils.getCurrentUser();
        Assert.isTrue(currentUser.getId().equals(permissionRequest.getApproverId()), "无权审批此申请");

        // 3. 校验申请状态是否为“待审批”
        Assert.isTrue(permissionRequest.getStatus() == AccessPermissionRequestStatus.PENDING, "该申请已被处理，请勿重复操作");

        // 4. 校验传入的新状态是否合法
        AccessPermissionRequestStatus newStatus = approveDto.getStatus();
        Assert.isTrue(newStatus == AccessPermissionRequestStatus.APPROVED || newStatus == AccessPermissionRequestStatus.REJECTED,
                "无效的审批状态: {}", newStatus);

        // 5. 更新状态和时间
        permissionRequest.setStatus(newStatus);
        permissionRequest.setUpdatedAt(LocalDateTime.now());

        if (newStatus == AccessPermissionRequestStatus.APPROVED) {
            // 6. 如果同意，调用第三方门禁平台接口
            log.info("权限申请 {} 已被用户 {} 同意。正在调用第三方门禁平台...", permissionRequestId, currentUser.getUsername());
            callThirdPartyAccessControlSystem(permissionRequest.getAccessControlId(), permissionRequest.getWorkOrderId());
            log.info("第三方门禁平台调用成功，权限已授予。");
        } else {
            // 7. 如果拒绝，记录日志
            log.warn("权限申请 {} 已被用户 {} 拒绝。原因: {}", permissionRequestId, currentUser.getUsername(), approveDto.getRemark());
        }

        // 8. 保存更新后的申请记录
        accessPermissionRequestMapper.updateById(permissionRequest);

        return permissionRequest;
    }

    /**
     * 模拟调用第三方门禁平台接口
     * @param accessControlId 门禁ID
     * @param workOrderId 工单ID
     */
    private void callThirdPartyAccessControlSystem(Long accessControlId, Long workOrderId) {
        // 在真实场景中，这里会包含调用第三方API的逻辑，例如使用RestTemplate或FeignClient
        // 模拟操作耗时
        try {
            Thread.sleep(500); // 模拟网络延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("模拟调用第三方平台时发生中断", e);
        }
        log.info("模拟：已成功为工单 {} 在门禁 {} 上开通权限。", workOrderId, accessControlId);
    }
}