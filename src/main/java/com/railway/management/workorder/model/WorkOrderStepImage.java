package com.railway.management.workorder.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("work_order_step_image")
public class WorkOrderStepImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的工单ID
     */
    private Long workOrderId;

    /**
     * 作业指导书的步骤编号 (例如: 1, 2, 3...)
     * 假设作业指导书的步骤是固定的
     */
    private Integer stepNumber;

    /**
     * 作业指导书的步骤描述
     */
    private String stepDescription;

    /**
     * 上传的图片URL
     */
    private String imageUrl;

    /**
     * 上传人ID
     */
    private Long uploadedBy;

    /**
     * 上传人姓名
     */
    private String uploaderName;

    /**
     * 上传时间
     */
    private LocalDateTime createdAt;
}