package com.railway.management.workorder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.workorder.dto.*;
import com.railway.management.workorder.model.WorkOrder;
import com.railway.management.workorder.model.WorkOrderStepImage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface WorkOrderService {

    /**
     * 创建工单
     *
     * @param request 创建工单的请求参数
     * @return 创建的工单实体
     */
    WorkOrder createWorkOrder(CreateWorkOrderRequest request);

    void deleteWorkOrder(Long workOrderId);

    /**
     * 查询工单列表
     *
     * @param query 查询参数
     * @param page  分页参数
     * @return 分页后的工单列表
     */
    IPage<WorkOrderListResponse> listWorkOrders(WorkOrderQueryRequest query, Page<WorkOrder> page);

    /**
     * 上传并保存工单步骤图片
     *
     * @param workOrderId     工单ID
     * @param stepNumber      步骤编号
     * @param stepDescription 步骤描述
     * @param file            图片文件
     * @return 保存后的图片记录实体
     */
    WorkOrderStepImage uploadAndSaveStepImage(Long workOrderId, Integer stepNumber, String stepDescription, MultipartFile file) throws IOException;

    /**
     * 更新工单状态
     *
     * @param workOrderId 工单ID
     * @param request     更新状态的请求体
     * @return 更新后的工单实体
     */
    WorkOrder updateWorkOrderStatus(Long workOrderId, UpdateWorkOrderStatusRequest request);

    /**
     * 记录工单出工
     *
     * @param request 记录出工的请求参数
     */
    void recordAttendance(RecordAttendanceRequest request);

    /**
     * 分页查询工单列表
     * @param page 分页对象
     * @param queryDto 查询条件
     */
    IPage<WorkOrder> getPage(Page<WorkOrder> page, WorkOrderQueryDto queryDto);

    List<WorkOrderStepImage> getStepImagesByWorkOrderId(Long workOrderId);

    // 在 WorkOrderService 中添加统计方法
    Map<String, Object> getWorkOrderStatistics();
}