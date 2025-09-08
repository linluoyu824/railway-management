package com.railway.management.workorder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.workorder.dto.CreateWorkOrderRequest;
import com.railway.management.workorder.dto.WorkOrderListResponse;
import com.railway.management.workorder.dto.WorkOrderQueryRequest;
import com.railway.management.workorder.dto.UpdateWorkOrderStatusRequest;
import com.railway.management.workorder.model.WorkOrder;
import com.railway.management.workorder.model.WorkOrderStepImage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface WorkOrderService {
    /**
     * 创建工单
     * @param request 创建工单的请求体
     * @return 创建的工单实体
     */
    WorkOrder createWorkOrder(CreateWorkOrderRequest request);

    /**
     * 更新工单状态
     * @param workOrderId 工单ID
     * @param request 更新状态的请求体
     * @return 更新后的工单实体
     */
    WorkOrder updateWorkOrderStatus(Long workOrderId, UpdateWorkOrderStatusRequest request);

    /**
     * 删除工单
     * @param workOrderId 工单ID
     */
    void deleteWorkOrder(Long workOrderId);

    /**
     * 查询工单列表
     * @param query 查询参数
     * @param page 分页参数
     * @return 分页后的工单列表
     */
    IPage<WorkOrderListResponse> listWorkOrders(WorkOrderQueryRequest query, Page<WorkOrder> page);

    /**
     * 上传并保存工单步骤图片
     * @param workOrderId 工单ID
     * @param stepNumber 步骤编号
     * @param stepDescription 步骤描述
     * @param file 图片文件
     * @return 保存后的图片记录实体
     */
    WorkOrderStepImage uploadAndSaveStepImage(Long workOrderId, Integer stepNumber, String stepDescription, MultipartFile file) throws IOException;
}