package com.railway.management.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.equipment.dto.EquipmentImportDto;
import com.railway.management.equipment.model.Equipment;
import com.railway.management.equipment.model.EquipmentStatus;
import com.railway.management.equipment.model.Parameter;
import com.railway.management.equipment.service.EquipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
public class EquipmentImportListener extends AnalysisEventListener<EquipmentImportDto> {

    private final EquipmentService equipmentService;
    private final ObjectMapper objectMapper;
    private final ExcelImportResult<EquipmentImportDto> result;

    public EquipmentImportListener(EquipmentService equipmentService, ObjectMapper objectMapper, ExcelImportResult<EquipmentImportDto> result) {
        this.equipmentService = equipmentService;
        this.objectMapper = objectMapper;
        this.result = result;
    }

    @Override
    public void invoke(EquipmentImportDto data, AnalysisContext context) {
        try {
            Equipment equipment = new Equipment();
            equipment.setName(data.getName());
            equipment.setType(data.getType());
            equipment.setSerialNumber(data.getSerialNumber());
            equipment.setPurchaseDate(data.getPurchaseDate());
            equipment.setAdminUserId(data.getAdminUserId());

            if (StringUtils.hasText(data.getStatus())) {
                equipment.setStatus(EquipmentStatus.valueOf(data.getStatus().toUpperCase()));
            } else {
                equipment.setStatus(EquipmentStatus.IN_USE);
            }

            if (StringUtils.hasText(data.getParametersJson())) {
                List<Parameter> parameters = objectMapper.readValue(data.getParametersJson(), new TypeReference<>() {});
                equipment.setParameters(parameters);
            }

            equipmentService.save(equipment);
            result.incrementSuccessCount(1);
        } catch (Exception e) {
            log.error("导入设备失败，行数据: {}, 错误: {}", data, e.getMessage());
            result.addFailure(data, e.getMessage());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("设备导入分析完成. 成功: {}, 失败: {}", result.getSuccessCount(), result.getFailedRows().size());
    }
}