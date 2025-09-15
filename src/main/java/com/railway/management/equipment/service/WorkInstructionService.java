package com.railway.management.equipment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.railway.management.equipment.dto.WorkInstructionDto;
import com.railway.management.equipment.dto.WorkInstructionUpdateDto;
import com.railway.management.equipment.model.WorkInstruction;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface WorkInstructionService extends IService<WorkInstruction> {
    /**
     * 上传文件并创建作业指导书
     */
    WorkInstructionDto createWorkInstruction(String name, MultipartFile file) throws IOException;

    /**
     * 更新作业指导书信息
     */
    WorkInstructionDto updateWorkInstruction(Long id, WorkInstructionUpdateDto updateDto);

    /**
     * 删除作业指导书
     */
    void deleteWorkInstruction(Long id);

    /**
     * 分页查询作业指导书
     */
    IPage<WorkInstructionDto> getWorkInstructionPage(IPage<WorkInstruction> page, String name);

    /**
     * 根据ID获取DTO
     */
    WorkInstructionDto getWorkInstructionDtoById(Long id);
}