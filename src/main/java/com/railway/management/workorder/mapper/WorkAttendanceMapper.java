package com.railway.management.workorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.workorder.model.WorkAttendance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单出工记录 Mapper 接口
 */
@Mapper
public interface WorkAttendanceMapper extends BaseMapper<WorkAttendance> {
}