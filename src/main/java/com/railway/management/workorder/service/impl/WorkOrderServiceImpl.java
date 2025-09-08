package com.railway.management.workorder.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.workorder.dto.CreateWorkOrderRequest;
import com.railway.management.workorder.dto.WorkOrderListResponse;
import com.railway.management.workorder.dto.WorkOrderQueryRequest;
import com.railway.management.workorder.dto.UpdateWorkOrderStatusRequest;
import com.railway.management.workorder.mapper.WorkOrderMapper;
import com.railway.management.workorder.mapper.WorkOrderStepImageMapper;
import com.railway.management.workorder.model.WorkOrder;
import com.railway.management.workorder.model.WorkOrderStatus;
import com.railway.management.workorder.model.WorkOrderStepImage;
import com.railway.management.workorder.service.WorkOrderService;
import com.railway.management.common.user.model.User;
import com.railway.management.common.user.service.UserService;
import com.railway.management.equipment.model.Equipment;
import com.railway.management.equipment.model.WorkInstruction; // 假设存在 WorkInstruction 模型
import com.railway.management.equipment.service.EquipmentService;
import com.railway.management.equipment.model.Tool; // 假设存在 Tool 模型
import lombok.RequiredArgsConstructor;
import com.railway.management.storage.service.FileStorageService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import com.railway.management.utils.SecurityUtils;
import com.railway.management.equipment.service.ToolService;

@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderStepImageMapper workOrderStepImageMapper;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final EquipmentService equipmentService;
    private final ToolService toolService;

    @Override
    @Transactional
    public WorkOrder createWorkOrder(CreateWorkOrderRequest request) {
        // 1. 获取当前用户信息 (发起人)
        User currentUser = SecurityUtils.getCurrentUser();

        // 2. 获取设备信息和关联的工作指导文件
        Equipment equipment = equipmentService.getById(request.getEquipmentId());
        Assert.notNull(equipment, "设备不存在: " + request.getEquipmentId());

        // 假设 Equipment 实体关联了 WorkInstruction
        WorkInstruction instruction = equipment.getWorkInstruction();
        Assert.notNull(instruction, "设备 " + equipment.getName() + " 没有关联的作业指导书");

        // 3. 获取维修人员信息
        List<User> assignees = userService.listByIds(request.getAssigneeIds());
        if (assignees.size() != request.getAssigneeIds().size()) {
            throw new RuntimeException("部分指定的维修人员不存在");
        }
        String assigneeIdsStr = assignees.stream().map(u -> u.getId().toString()).collect(Collectors.joining(","));
        String assigneeNamesStr = assignees.stream().map(User::getUsername).collect(Collectors.joining(", "));

        // 3.5. 获取工具信息
        List<Tool> tools = toolService.getByIds(request.getToolIds());
        if (tools.size() != request.getToolIds().size()) {
            throw new RuntimeException("部分指定的工具不存在");
        }
        String toolNames = tools.stream().map(Tool::getName).collect(Collectors.joining(", "));

        // 4. 构建工单实体
        WorkOrder workOrder = new WorkOrder()
                .setEquipmentId(request.getEquipmentId())
                .setEquipmentName(equipment.getName())
                .setDescription(request.getDescription())
                .setCreatorId(currentUser.getId())
                .setCreatorName(currentUser.getUsername())
                .setAssigneeIds(assigneeIdsStr)
                .setAssigneeNames(assigneeNamesStr)
                .setWorkInstructionId(instruction.getId())
                .setWorkInstructionName(instruction.getName())
                .setWorkInstructionUrl(instruction.getUrl())
                .setTools(toolNames)
                .setStatus(WorkOrderStatus.DRAFT) // 新创建的工单默认为草稿状态
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());

        // 5. 保存到数据库
        workOrderMapper.insert(workOrder);

        return workOrder;
    }

    @Override
    @Transactional
    public WorkOrder updateWorkOrderStatus(Long workOrderId, UpdateWorkOrderStatusRequest request) {
        // 1. 获取工单
        WorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            // 在实际项目中，建议使用自定义的业务异常
            throw new RuntimeException("工单不存在: " + workOrderId);
        }

        WorkOrderStatus newStatus = request.getNewStatus();

        // 2. 获取当前用户角色
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> userRoles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // 3. 权限与状态流转校验
        if (newStatus == WorkOrderStatus.APPROVED) {
            // 变更为“审批通过”时，需要管理员或调度中心人员
            if (userRoles.stream().noneMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_DISPATCHER"))) {
                throw new AccessDeniedException("您没有权限审批此工单");
            }
            // 状态流转校验：只有“审批中”的工单才能被“审批通过”
            if (workOrder.getStatus() != WorkOrderStatus.IN_APPROVAL) {
                throw new IllegalStateException("只有'审批中'的工单才能被审批通过");
            }
        } else {
            // 其他状态变更，需要班组长
            if (!userRoles.contains("ROLE_TEAM_LEADER")) {
                throw new AccessDeniedException("您没有权限变更此工单状态");
            }
        }

        // 4. 更新工单
        workOrder.setStatus(newStatus);
        workOrder.setUpdatedAt(LocalDateTime.now());

        // 如果工单完成，记录完成时间
        if (newStatus == WorkOrderStatus.WORK_ORDER_COMPLETED) {
            workOrder.setCompletedAt(LocalDateTime.now());
        }

        // 5. 保存到数据库
        workOrderMapper.updateById(workOrder);

        return workOrder;
    }

    @Override
    @Transactional
    public void deleteWorkOrder(Long workOrderId) {
        // 1. 获取工单
        WorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new RuntimeException("工单不存在: " + workOrderId);
        }

        // 2. 状态校验：只有草稿状态的工单可以被删除
        if (workOrder.getStatus() != WorkOrderStatus.DRAFT) {
            throw new IllegalStateException("只有'草稿'状态的工单才能被删除");
        }

        // 3. 执行删除 (权限已在Controller层通过@PreAuthorize校验)
        workOrderMapper.deleteById(workOrderId);
    }

    @Override
    public IPage<WorkOrderListResponse> listWorkOrders(WorkOrderQueryRequest query, Page<WorkOrder> page) {
        QueryWrapper<WorkOrder> wrapper = new QueryWrapper<>();

        // 构建查询条件
        wrapper.like(StrUtil.isNotBlank(query.getCreatorName()), "creator_name", query.getCreatorName());
        wrapper.like(StrUtil.isNotBlank(query.getAssigneeNames()), "assignee_name", query.getAssigneeNames());
        wrapper.eq(query.getStatus() != null, "status", query.getStatus());
        wrapper.between(query.getStartTime() != null && query.getEndTime() != null,
                "created_at", query.getStartTime(), query.getEndTime());

        // 默认按创建时间降序排序
        wrapper.orderByDesc("created_at");

        IPage<WorkOrder> workOrderPage = workOrderMapper.selectPage(page, wrapper);

        // 映射为响应DTO
        return workOrderPage.convert(WorkOrderListResponse::new);
    }

    @Override
    @Transactional
    public WorkOrderStepImage uploadAndSaveStepImage(Long workOrderId, Integer stepNumber, String stepDescription, MultipartFile file) throws IOException {
        // 1. 基础校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        WorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new RuntimeException("工单不存在: " + workOrderId);
        }

        // 2. 状态校验：确保工单处于“维修中”或“维修完成”状态
        if (workOrder.getStatus() != WorkOrderStatus.IN_PROGRESS && workOrder.getStatus() != WorkOrderStatus.REPAIR_COMPLETED) {
            throw new IllegalStateException("当前工单状态不允许上传图片");
        }

        // 3. 权限校验：检查当前操作员是否是该工单的维修人之一
        User currentUser = SecurityUtils.getCurrentUser();

        String[] assigneeIds = workOrder.getAssigneeIds().split(",");
        if (Arrays.stream(assigneeIds).map(String::trim).noneMatch(id -> id.equals(String.valueOf(currentUser.getId())))) {
            throw new AccessDeniedException("您不是该工单的维修人员，无权上传");
        }

        // 4. 上传文件到存储服务
        String imageUrl = fileStorageService.upload(file);

        // 5. 创建并保存图片记录到数据库
        WorkOrderStepImage stepImage = new WorkOrderStepImage();
        stepImage.setWorkOrderId(workOrderId);
        stepImage.setStepNumber(stepNumber);
        stepImage.setStepDescription(stepDescription);
        stepImage.setImageUrl(imageUrl);
        stepImage.setUploadedBy(currentUser.getId());
        stepImage.setUploaderName(currentUser.getName()); // 确保与 createWorkOrder 一致
        stepImage.setCreatedAt(LocalDateTime.now());

        workOrderStepImageMapper.insert(stepImage);

        return stepImage;
    }
}