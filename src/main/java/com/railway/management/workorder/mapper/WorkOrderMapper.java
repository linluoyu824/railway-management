package com.railway.management.workorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.workorder.model.WorkOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {
}