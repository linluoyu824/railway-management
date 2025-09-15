package com.railway.management.equipment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.railway.management.equipment.mapper.ParameterMapper;
import com.railway.management.equipment.model.Parameter;
import com.railway.management.equipment.service.ParameterService;
import org.springframework.stereotype.Service;

@Service
public class ParameterServiceImpl extends ServiceImpl<ParameterMapper, Parameter> implements ParameterService {
}