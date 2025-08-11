package com.railway.management.common.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.common.equipment.model.AccessControl;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门禁设备 Mapper 接口
 */
@Mapper
public interface AccessControlMapper extends BaseMapper<AccessControl> {
}