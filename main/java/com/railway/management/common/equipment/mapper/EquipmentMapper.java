package com.railway.management.common.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.common.equipment.model.Equipment;
import com.railway.management.common.equipment.dto.EquipmentDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
    /**
     * 根据ID查询设备及其管理人员的详细信息
     * @param id 设备ID
     */
    EquipmentDetailDto selectDetailById(@Param("id") Long id);

    /**
     * 根据管理员ID查询其管理的所有设备详细信息列表
     * @param adminUserId 管理员ID
     * @return 设备详情DTO列表
     */
    List<EquipmentDetailDto> selectDetailsByAdminUserId(@Param("adminUserId") Long adminUserId,@Param("departmentPath") String departmentPath);
}