package com.railway.management.common.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.common.equipment.model.AccessControlLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门禁操作记录 Mapper 接口
 */
@Mapper
public interface AccessControlLogMapper extends BaseMapper<AccessControlLog> {
}