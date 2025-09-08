package com.railway.management.common.section.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.railway.management.common.department.mapper.DepartmentMapper;
import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.common.section.dto.SectionCreateDto;
import com.railway.management.common.section.dto.SectionImportDto;
import com.railway.management.common.section.dto.SectionUpdateDto;
import com.railway.management.common.section.mapper.SectionMapper;
import com.railway.management.common.section.model.Section;
import com.railway.management.common.section.service.SectionService;
import com.railway.management.common.user.mapper.UserMapper;
import com.railway.management.utils.SectionImportListener;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl extends ServiceImpl<SectionMapper, Section> implements SectionService {

    private final SectionMapper sectionMapper;
    private final UserMapper userMapper;
    private DepartmentMapper departmentMapper;

    @Override
    public Section createSection(SectionCreateDto createDto) {
        Section section = new Section();
        section.setName(createDto.getName());
        section.setDescription(createDto.getDescription());
        section.setManagerId(createDto.getManagerId());
        section.setDepartmentId(createDto.getDepartmentId());

        sectionMapper.insert(section);
        return section;
    }

    @Override
    public Section updateSection(SectionUpdateDto updateDto) {
        Section section = sectionMapper.selectById(updateDto.getId());

        if (section == null) {
            throw new IllegalArgumentException("Section not found with id: " + updateDto.getId());
        }

        section.setName(updateDto.getName());
        section.setDescription(updateDto.getDescription());
        section.setManagerId(updateDto.getManagerId());

        sectionMapper.updateById(section);
        return section;
    }

    @Override
    public IPage<Section> listSections(IPage<Section> page) {
        return sectionMapper.selectPage(page, null);
    }


        @Override
    @Transactional
    public ExcelImportResult<SectionImportDto> importSections(InputStream inputStream) {
        ExcelImportResult<SectionImportDto> result = new ExcelImportResult<>();
        SectionImportListener listener = new SectionImportListener(baseMapper, userMapper, departmentMapper, result);
        EasyExcel.read(inputStream, SectionImportDto.class, listener).sheet().doRead();
        return result;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        String fileName = "区段导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), SectionImportDto.class).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("区段模板").build();
            excelWriter.write(Collections.emptyList(), writeSheet);
        }
    }
}



