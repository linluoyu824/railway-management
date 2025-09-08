package com.railway.management.workorder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.workorder.dto.CreateWorkOrderRequest;
import com.railway.management.workorder.dto.WorkOrderListResponse;
import com.railway.management.workorder.dto.WorkOrderQueryRequest;
import com.railway.management.workorder.dto.UpdateWorkOrderStatusRequest;
import com.railway.management.workorder.model.WorkOrder;
import com.railway.management.workorder.model.WorkOrderStepImage;
import com.railway.management.workorder.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/workorders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    /**
     * 创建工单 - 仅限班组长角色
     * @param request 请求体
     * @return 创建的工单信息
     */
    @PostMapping
    @PreAuthorize("hasRole('TEAM_LEADER')") // 权限控制，假设班组长角色为 'TEAM_LEADER'
    public ResponseEntity<WorkOrder> createWorkOrder(@Validated @RequestBody CreateWorkOrderRequest request) {
        WorkOrder createdWorkOrder = workOrderService.createWorkOrder(request);
        return ResponseEntity.ok(createdWorkOrder);
    }

    /**
     * 更新工单状态
     * @param id 工单ID
     * @param request 请求体
     * @return 更新后的工单信息
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()") // 确保用户已登录
    public ResponseEntity<WorkOrder> updateWorkOrderStatus(@PathVariable("id") Long id, @Validated @RequestBody UpdateWorkOrderStatusRequest request) {
        WorkOrder updatedWorkOrder = workOrderService.updateWorkOrderStatus(id, request);
        return ResponseEntity.ok(updatedWorkOrder);
    }

    /**
     * 删除工单 - 仅限草稿状态
     * @param id 工单ID
     * @return No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEAM_LEADER')") // 仅限班组长删除
    public ResponseEntity<Void> deleteWorkOrder(@PathVariable("id") Long id) {
        workOrderService.deleteWorkOrder(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 查询工单列表
     * @param page 分页参数 (e.g., ?current=1&size=10)
     * @param query 查询参数
     * @return 分页后的工单列表
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IPage<WorkOrderListResponse>> listWorkOrders(Page<WorkOrder> page, WorkOrderQueryRequest query) {
        IPage<WorkOrderListResponse> result = workOrderService.listWorkOrders(query, page);
        return ResponseEntity.ok(result);
    }

    /**
     * 为工单的特定步骤上传图片
     * @param workOrderId 工单ID
     * @param stepNumber 步骤编号
     * @param stepDescription 步骤描述
     * @param file 图片文件
     * @return 上传的图片信息
     */
    @PostMapping("/{workOrderId}/step-images")
    @PreAuthorize("isAuthenticated()") // 具体权限在Service层校验
    public ResponseEntity<WorkOrderStepImage> uploadStepImage(
            @PathVariable Long workOrderId,
            @RequestParam Integer stepNumber,
            @RequestParam String stepDescription,
            @RequestParam("file") MultipartFile file) throws IOException {

        WorkOrderStepImage stepImage = workOrderService.uploadAndSaveStepImage(
                workOrderId, stepNumber, stepDescription, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(stepImage);
    }
}