package com.railway.management.equipment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.railway.management.equipment.mapper.ToolMapper;
import com.railway.management.equipment.model.Tool;
import com.railway.management.equipment.service.ToolService;
import org.springframework.stereotype.Service;

@Service
public class ToolServiceImpl extends ServiceImpl<ToolMapper, Tool> implements ToolService {
    // getByIds 方法已在接口中作为默认方法实现。
    // 未来可以在此处添加更多工具相关的业务逻辑。
}