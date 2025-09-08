package com.railway.management.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.railway.management.common.department.mapper.DepartmentMapper;
import com.railway.management.common.dto.ExcelImportResult;

import com.railway.management.common.section.dto.SectionImportDto;
import com.railway.management.common.section.mapper.SectionMapper;
import com.railway.management.common.section.model.Section;
import com.railway.management.common.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Slf4j
public class SectionImportListener extends AnalysisEventListener<SectionImportDto> {

    private final SectionMapper sectionMapper;
    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final ExcelImportResult<SectionImportDto> result;

    public SectionImportListener(SectionMapper sectionMapper, UserMapper userMapper, DepartmentMapper departmentMapper, ExcelImportResult<SectionImportDto> result) {
        this.sectionMapper = sectionMapper;
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
        this.result = result;
    }

    @Override
    public void invoke(SectionImportDto data, AnalysisContext context) {
        try {
            StringBuilder errorMsg = new StringBuilder();

            // 1. 基础校验
            if (!StringUtils.hasText(data.getName())) {
                errorMsg.append("区段名称不能为空; ");
            }
            if (data.getManagerId() == null) {
                errorMsg.append("管理人ID不能为空; ");
            }
            if (data.getDepartmentId() == null) {
                errorMsg.append("所属部门ID不能为空; ");
            }
            if (data.getMileage() == null || data.getMileage().compareTo(BigDecimal.ZERO) < 0) {
                errorMsg.append("区段里程必须为非负数; ");
            }

            // 如果基础校验有误，直接记录失败并返回，避免后续空指针
            if (errorMsg.length() > 0) {
                result.addFailure(data, errorMsg.toString().trim());
                return;
            }

            // 2. 业务校验
            if (userMapper.selectById(data.getManagerId()) == null) {
                errorMsg.append("管理人ID '").append(data.getManagerId()).append("' 不存在; ");
            }
            if (departmentMapper.selectById(data.getDepartmentId()) == null) {
                errorMsg.append("所属部门ID '").append(data.getDepartmentId()).append("' 不存在; ");
            }
            if (sectionMapper.exists(new QueryWrapper<Section>().eq("name", data.getName()).eq("department_id", data.getDepartmentId()))) {
                errorMsg.append("部门 '").append(data.getDepartmentId()).append("' 下已存在名为 '").append(data.getName()).append("' 的区段; ");
            }

            if (errorMsg.length() > 0) {
                result.addFailure(data, errorMsg.toString().trim());
            } else {
                // 3. 创建并保存
                Section section = convertToEntity(data);
                sectionMapper.insert(section);
                result.incrementSuccessCount(1);
            }
        } catch (Exception e) {
            log.error("导入区段失败，行数据: {}, 错误: {}", data, e.getMessage());
            result.addFailure(data, e.getMessage());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("区段导入分析完成. 成功: {}, 失败: {}", result.getSuccessCount(), result.getFailedRows().size());
    }

    private Section convertToEntity(SectionImportDto dto) {
        Section section = new Section();
        section.setName(dto.getName());
        section.setDescription(dto.getDescription());
        section.setMileage(dto.getMileage());
        section.setStartPoint(dto.getStartPoint());
        section.setEndPoint(dto.getEndPoint());
        section.setManagerId(dto.getManagerId());
        section.setDepartmentId(dto.getDepartmentId());
        return section;
    }
}