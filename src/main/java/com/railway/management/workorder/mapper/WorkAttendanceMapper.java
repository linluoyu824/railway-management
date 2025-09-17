package com.railway.management.workorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.workorder.model.WorkAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工单出工记录 Mapper 接口
 */
@Mapper
public interface WorkAttendanceMapper extends BaseMapper<WorkAttendance> {
    /**
     * 批量插入出工记录
     * @param list 出工记录列表
     */
    void insertBatch(@Param("list") List<WorkAttendance> list);

    /**
     * 统计指定工单中已打卡的独立用户数量
     * @param workOrderId 工单ID
     * @param userIds     需要检查的用户ID列表
     * @return 已打卡的独立用户数量
     */
    Long countDistinctClockedInUsers(@Param("workOrderId") Long workOrderId, @Param("userIds") List<Long> userIds);
}