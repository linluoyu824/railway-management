package com.railway.management.common.section.service;

import java.io.IOException;
import java.io.InputStream;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.common.section.dto.SectionCreateDto;
import com.railway.management.common.section.dto.SectionImportDto;
import com.railway.management.common.section.dto.SectionUpdateDto;
import com.railway.management.common.section.model.Section;

import jakarta.servlet.http.HttpServletResponse;

public interface SectionService extends IService<Section> {

    /**
     * 创建区段
     */
    Section createSection(SectionCreateDto createDto);

    /**
     * 更新区段
     */
    Section updateSection(SectionUpdateDto updateDto);

    /**
     * 获取区段列表（分页）
     */
    IPage<Section> listSections(IPage<Section> page);

    /**
     * 从Excel批量导入区段
     * @param inputStream Excel文件输入流
     * @return 导入结果
     */
    ExcelImportResult<SectionImportDto> importSections(InputStream inputStream);

    /**
     * 下载区段导入模板
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;

}