package com.railway.management.workorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.workorder.model.WorkOrderAssignee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkOrderAssigneeMapper extends BaseMapper<WorkOrderAssignee> {
    /**
     * 批量插入工单与人员的关联关系
     */
    void insertBatch(@Param("list") List<WorkOrderAssignee> list);
}