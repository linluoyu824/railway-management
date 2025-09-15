package com.railway.management.equipment.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.railway.management.equipment.dto.WorkInstructionDto;
import com.railway.management.equipment.dto.WorkInstructionUpdateDto;
import com.railway.management.equipment.mapper.WorkInstructionMapper;
import com.railway.management.equipment.model.WorkInstruction;
import com.railway.management.equipment.service.WorkInstructionService;
import com.railway.management.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class WorkInstructionServiceImpl extends ServiceImpl<WorkInstructionMapper, WorkInstruction> implements WorkInstructionService {

    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public WorkInstructionDto createWorkInstruction(String name, MultipartFile file) throws IOException {
        Assert.notBlank(name, "作业指导书名称不能为空");
        Assert.isFalse(file == null || file.isEmpty(), "上传文件不能为空");

        String fileUrl = fileStorageService.upload(file);

        WorkInstruction instruction = new WorkInstruction();
        instruction.setName(name);
        instruction.setUrl(fileUrl);
        this.save(instruction);

        return convertToDto(instruction);
    }

    @Override
    @Transactional
    public WorkInstructionDto updateWorkInstruction(Long id, WorkInstructionUpdateDto updateDto) {
        WorkInstruction instruction = this.getById(id);
        Assert.notNull(instruction, "作业指导书不存在: {}", id);

        instruction.setName(updateDto.getName());
        this.updateById(instruction);

        return convertToDto(instruction);
    }

    @Override
    @Transactional
    public void deleteWorkInstruction(Long id) {
        // 建议：未来可以增加从文件存储中删除物理文件的逻辑
        // fileStorageService.delete(instruction.getUrl());
        this.removeById(id);
    }

    @Override
    public IPage<WorkInstructionDto> getWorkInstructionPage(IPage<WorkInstruction> page, String name) {
        QueryWrapper<WorkInstruction> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), "name", name).orderByDesc("id");
        return this.page(page, wrapper).convert(this::convertToDto);
    }

    @Override
    public WorkInstructionDto getWorkInstructionDtoById(Long id) {
        return convertToDto(this.getById(id));
    }

    private WorkInstructionDto convertToDto(WorkInstruction instruction) {
        if (instruction == null) return null;
        WorkInstructionDto dto = new WorkInstructionDto();
        BeanUtils.copyProperties(instruction, dto);
        return dto;
    }
}