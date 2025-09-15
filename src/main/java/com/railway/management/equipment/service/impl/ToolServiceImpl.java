package com.railway.management.equipment.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.railway.management.common.user.model.User;
import com.railway.management.equipment.dto.ToolCreateNfcDto;
import com.railway.management.equipment.mapper.ToolLogMapper;
import com.railway.management.equipment.mapper.ToolMapper;
import com.railway.management.equipment.model.Tool;
import com.railway.management.equipment.model.ToolLog;
import com.railway.management.equipment.model.ToolStatus;
import com.railway.management.equipment.service.ToolService;
import com.railway.management.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class ToolServiceImpl extends ServiceImpl<ToolMapper, Tool> implements ToolService {
    // getByIds 方法已在接口中作为默认方法实现。
    // 未来可以在此处添加更多工具相关的业务逻辑。

    private final ToolMapper toolMapper;
    private final ToolLogMapper toolLogMapper;

    @Override
    @Transactional
    public ToolLog processNfcScan(String nfcId) {
        Assert.notBlank(nfcId, "NFC ID不能为空");

        // 1. 根据NFC ID查找工具
        Tool tool = toolMapper.selectOne(new QueryWrapper<Tool>().eq("nfc_id", nfcId));
        Assert.notNull(tool, "未找到NFC ID为 {} 的工具", nfcId);

        // 2. 获取当前用户
        User currentUser = SecurityUtils.getCurrentUser();

        // 3. 根据工具当前状态决定操作 (借出/归还)
        String action;
        if (tool.getStatus() == ToolStatus.IN_STOCK) {
            tool.setStatus(ToolStatus.IN_USE);
            action = "BORROW"; // 借出
        } else if (tool.getStatus() == ToolStatus.IN_USE) {
            tool.setStatus(ToolStatus.IN_STOCK);
            action = "RETURN"; // 归还
        } else {
            throw new IllegalStateException("工具状态异常，无法执行操作: " + tool.getStatus());
        }
        toolMapper.updateById(tool);

        // 4. 记录操作日志
        ToolLog toolLog = new ToolLog();
        toolLog.setToolId(tool.getId());
        toolLog.setUserId(currentUser.getId());
        toolLog.setUsername(currentUser.getUsername());
        toolLog.setAction(action);
        toolLog.setTimestamp(LocalDateTime.now());
        toolLogMapper.insert(toolLog);

        return toolLog;
    }

    @Override
    @Transactional
    public Tool createFromNfc(ToolCreateNfcDto dto) {
        Assert.notBlank(dto.getNfcId(), "NFC ID不能为空");
        Assert.notBlank(dto.getName(), "新工具名称不能为空");

        // 1. Check for duplicates
        Assert.isFalse(this.exists(new QueryWrapper<Tool>().eq("nfc_id", dto.getNfcId())),
                "NFC ID {} 已被注册", dto.getNfcId());

        // 2. Create and save
        Tool tool = new Tool();
        tool.setNfcId(dto.getNfcId());
        tool.setName(dto.getName());
        tool.setModel(dto.getModel());
        tool.setStatus(ToolStatus.IN_STOCK);
        this.save(tool);

        log.info("通过NFC成功注册新工具: {}", tool.getName());
        return tool;
    }
}