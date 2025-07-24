package com.railway.management.position.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.position.model.Position;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PositionMapper extends BaseMapper<Position> {
    // 复杂的查询可以写在这里，或者在XML中
}