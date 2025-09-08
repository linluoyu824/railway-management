package com.railway.management.equipment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.railway.management.equipment.mapper.WorkInstructionMapper;
import com.railway.management.equipment.model.WorkInstruction;
import com.railway.management.equipment.service.WorkInstructionService;
import org.springframework.stereotype.Service;

@Service
public class WorkInstructionServiceImpl extends ServiceImpl<WorkInstructionMapper, WorkInstruction> implements WorkInstructionService {
}