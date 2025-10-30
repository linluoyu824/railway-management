package com.railway.management.workorder.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.common.user.model.User;
import com.railway.management.common.user.service.UserService;
import com.railway.management.equipment.model.Equipment;
import com.railway.management.equipment.model.Tool;
import com.railway.management.equipment.model.WorkInstruction;
import com.railway.management.equipment.service.EquipmentService;
import com.railway.management.equipment.service.ToolService;
import com.railway.management.storage.service.FileStorageService;
import com.railway.management.utils.SecurityUtils;
import com.railway.management.workorder.dto.*;
import com.railway.management.workorder.mapper.WorkAttendanceMapper;
import com.railway.management.workorder.mapper.WorkOrderAssigneeMapper;
import com.railway.management.workorder.mapper.WorkOrderMapper;
import com.railway.management.workorder.mapper.WorkOrderStepImageMapper;
import com.railway.management.workorder.model.*;
import com.railway.management.workorder.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderStepImageMapper workOrderStepImageMapper;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final EquipmentService equipmentService;
    private final ToolService toolService;
    private final WorkAttendanceMapper workAttendanceMapper;
    private final WorkOrderAssigneeMapper workOrderAssigneeMapper;

    @Override
    @Transactional
    public WorkOrder createWorkOrder(CreateWorkOrderRequest request) {
        // 1. 获取当前用户信息 (发起人)
        User currentUser = SecurityUtils.getCurrentUser();
        Assert.notNull(currentUser, "无法获取当前用户信息，请重新登录");

        // 2. 获取设备信息和关联的工作指导文件
        Equipment equipment = equipmentService.getById(request.getEquipmentId());
        Assert.notNull(equipment, "ID为 {} 的设备不存在", request.getEquipmentId());
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
        List<Tool> tools = toolService.listByIds(request.getToolIds());
        if (tools.size() != request.getToolIds().size()) {
            throw new RuntimeException("部分指定的工具不存在");
        }
        String toolNames = tools.stream().map(Tool::getName).collect(Collectors.joining(", "));

        // 4. 构建工单实体
        WorkOrder workOrder = new WorkOrder()
                .setType(request.getType())
                .setEquipmentId(request.getEquipmentId())
                .setEquipmentName(equipment.getName())
                .setDescription(request.getDescription())
                .setAssigneeIds(assigneeIdsStr)
                .setAssigneeNames(assigneeNamesStr)
                .setCreatorId(currentUser.getId())
                .setCreatorName(currentUser.getUsername())
                .setTools(toolNames)
                .setWorkInstructionId(instruction != null ? instruction.getId() : null)
                .setWorkInstructionName(instruction != null ? instruction.getName() : null)
                .setWorkInstructionUrl(instruction != null ? instruction.getUrl() : null)
                .setStatus(WorkOrderStatus.DRAFT)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());

        // 5. 保存到数据库
        workOrderMapper.insert(workOrder);

        // 6. 保存工单与人员的关联关系
        if (!assignees.isEmpty()) {
            List<WorkOrderAssignee> relations = assignees.stream()
                .map(assignee -> new WorkOrderAssignee(workOrder.getId(), assignee.getId()))
                .collect(Collectors.toList());
            workOrderAssigneeMapper.insertBatch(relations);
        }

        log.info("新工单已创建, ID: {}, 类型: {}", workOrder.getId(), workOrder.getType());

        return workOrder;
    }

    @Override
    @Transactional
    public void recordAttendance(RecordAttendanceRequest request) {
        // 1. 校验工单是否存在
        WorkOrder workOrder = workOrderMapper.selectById(request.getWorkOrderId());
        Assert.notNull(workOrder, "ID为 {} 的工单不存在", request.getWorkOrderId());

        // 2. 批量查询用户信息，以减少数据库查询次数
        List<User> users = userService.listByIds(request.getUserIds());
        Map<Long, String> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        // 3. 校验所有传入的userIds是否都有效
        for (Long userId : request.getUserIds()) {
            Assert.isTrue(userMap.containsKey(userId), "ID为 {} 的用户不存在", userId);
        }

        // 4. 遍历并创建出工记录
        List<WorkAttendance> attendanceList = request.getUserIds().stream().map(userId -> {
            WorkAttendance attendance = new WorkAttendance();
            attendance.setWorkOrderId(request.getWorkOrderId());
            attendance.setUserId(userId);
            attendance.setUsername(userMap.get(userId));
            attendance.setAttendanceType(request.getAttendanceType());
            attendance.setLocation(request.getLocation());
            attendance.setAttendanceTime(LocalDateTime.now());
            return attendance;
        }).collect(Collectors.toList());

        // 5. 批量插入出工记录
        if (!attendanceList.isEmpty()) {
            workAttendanceMapper.insert(attendanceList);
        }

        log.info("为工单ID {} 记录了 {} 条 {} 类型的出工记录", request.getWorkOrderId(), attendanceList.size(), request.getAttendanceType());
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
                .collect(Collectors.toList());

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
        stepImage.setUploaderName(currentUser.getUsername()); // 确保与 createWorkOrder 一致
        stepImage.setCreatedAt(LocalDateTime.now());
        workOrderStepImageMapper.insert(stepImage);

        return stepImage;
    }

    @Override
    public List<WorkOrderStepImage> getStepImagesByWorkOrderId(Long workOrderId) {
        Assert.notNull(workOrderId, "工单ID不能为空");

        // 使用QueryWrapper按工单ID查询，并按步骤编号升序排序
        return workOrderStepImageMapper.selectList(
                new QueryWrapper<WorkOrderStepImage>()
                        .eq("work_order_id", workOrderId)
                        .orderByAsc("step_number"));
    }

    @Override
    public IPage<WorkOrder> getPage(Page<WorkOrder> page, WorkOrderQueryDto queryDto) {
        QueryWrapper<WorkOrder> wrapper = new QueryWrapper<>();

        // 根据工单状态和类型进行筛选
        wrapper.eq(queryDto.getStatus() != null, "status", queryDto.getStatus());
        wrapper.eq(queryDto.getType() != null, "type", queryDto.getType());

        // 根据描述进行模糊查询
        // 建议：未来可以增加对设备名称、创建人等的筛选
        wrapper.like(StringUtils.hasText(queryDto.getDescription()), "description", queryDto.getDescription());

        // 增加更多查询条件
        wrapper.ge(queryDto.getStartTime() != null, "created_at", queryDto.getStartTime());
        wrapper.le(queryDto.getEndTime() != null, "created_at", queryDto.getEndTime());
        wrapper.like(StringUtils.hasText(queryDto.getCreatorName()), "creator_name", queryDto.getCreatorName());
        wrapper.like(StringUtils.hasText(queryDto.getEquipmentName()), "equipment_name", queryDto.getEquipmentName());


        // 按创建时间降序排序，确保最新的工单在前
        wrapper.orderByDesc("created_at");

        return workOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getWorkOrderStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 获取总工单数
        int totalWorkOrders = Math.toIntExact(workOrderMapper.selectCount(null));
        statistics.put("total", totalWorkOrders);

        // 按状态统计工单数
        Map<WorkOrderStatus, Integer> statusStatistics = new HashMap<>();
        for (WorkOrderStatus status : WorkOrderStatus.values()) {
            QueryWrapper<WorkOrder> wrapper = new QueryWrapper<>();
            wrapper.eq("status", status);
            int count = Math.toIntExact(workOrderMapper.selectCount(wrapper));
            statusStatistics.put(status, count);
        }
        statistics.put("byStatus", statusStatistics);

        // 按类型统计工单数
        Map<WorkOrderType, Integer> typeStatistics = new HashMap<>();
        for (WorkOrderType type : WorkOrderType.values()) {
            QueryWrapper<WorkOrder> wrapper = new QueryWrapper<>();
            wrapper.eq("type", type);
            int count = Math.toIntExact(workOrderMapper.selectCount(wrapper));
            typeStatistics.put(type, count);
        }
        statistics.put("byType", typeStatistics);

        // 获取今日创建的工单数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        QueryWrapper<WorkOrder> todayWrapper = new QueryWrapper<>();
        todayWrapper.between("created_at", todayStart, todayEnd);
        int todayWorkOrders = Math.toIntExact(workOrderMapper.selectCount(todayWrapper));
        statistics.put("today", todayWorkOrders);

        // 获取本周创建的工单数
        LocalDateTime weekStart = LocalDateTime.now().minusDays(LocalDateTime.now().getDayOfWeek().getValue() - 1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        QueryWrapper<WorkOrder> weekWrapper = new QueryWrapper<>();
        weekWrapper.between("created_at", weekStart, LocalDateTime.now());
        int weekWorkOrders = Math.toIntExact(workOrderMapper.selectCount(weekWrapper));
        statistics.put("thisWeek", weekWorkOrders);

        // 获取本月创建的工单数
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        QueryWrapper<WorkOrder> monthWrapper = new QueryWrapper<>();
        monthWrapper.between("created_at", monthStart, LocalDateTime.now());
        int monthWorkOrders = Math.toIntExact(workOrderMapper.selectCount(monthWrapper));
        statistics.put("thisMonth", monthWorkOrders);

        return statistics;
    }


}