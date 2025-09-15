package com.railway.management.common.train.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railway.management.common.train.model.Train;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrainMapper extends BaseMapper<Train> {
}