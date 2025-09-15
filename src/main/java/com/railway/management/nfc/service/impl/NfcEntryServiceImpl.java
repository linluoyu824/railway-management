package com.railway.management.nfc.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railway.management.equipment.dto.AccessControlCreateNfcDto;
import com.railway.management.equipment.dto.EquipmentCreateNfcDto;
import com.railway.management.equipment.dto.ToolCreateNfcDto;
import com.railway.management.equipment.model.AccessControlAction;
import com.railway.management.equipment.service.AccessControlService;
import com.railway.management.equipment.service.EquipmentService;
import com.railway.management.equipment.service.ToolService;
import com.railway.management.nfc.dto.NfcEntryRequest;
import com.railway.management.nfc.dto.NfcEntryType;
import com.railway.management.nfc.service.NfcEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NfcEntryServiceImpl implements NfcEntryService {

    // 假设您已经注入了处理各类业务的Service
    private final ToolService toolService;
    private final EquipmentService equipmentService;
    private final AccessControlService accessControlService;
    private final ObjectMapper objectMapper;

    @Override
    public Object processNfcEntry(NfcEntryRequest request) {
        try {
            JsonNode data = request.getData();
            log.info("接收到NFC录入请求, 类型: {}, 数据: {}", request.getType(), data.toString());

            switch (request.getType()) {
                case TOOL:
                    // 具体的业务逻辑，例如根据NFC ID查找工具并更新其状态（借出/归还）
                    String toolNfcId = data.get("nfcId").asText();
                    return toolService.processNfcScan(toolNfcId);

                case EQUIPMENT:
                    // 具体的业务逻辑，例如记录一次设备巡检
                    String equipmentNfcId = data.get("nfcId").asText();
                    return equipmentService.processNfcScan(equipmentNfcId);

                case ACCESS_CONTROL_IN:
                case ACCESS_CONTROL_OUT:
                    if (!data.has("accessControlId")) {
                        throw new IllegalArgumentException("门禁录入数据必须包含 'accessControlId'");
                    }
                    Long accessControlId = data.get("accessControlId").asLong();
                    AccessControlAction action = (request.getType() == NfcEntryType.ACCESS_CONTROL_IN)
                            ? AccessControlAction.ENTER
                            : AccessControlAction.EXIT;

                    // 建议: 在 AccessControlService 中创建一个新方法来处理此逻辑
                    // public AccessControlLog recordAccess(Long accessControlId, AccessControlAction action);
                    // 该方法内部通过 SecurityUtils.getCurrentUser() 获取用户，而不是从前端传递。
                    return accessControlService.recordAccess(accessControlId, action);

                case TOOL_REGISTRATION:
                    ToolCreateNfcDto toolDto = objectMapper.treeToValue(data, ToolCreateNfcDto.class);
                    return toolService.createFromNfc(toolDto);

                case EQUIPMENT_REGISTRATION:
                    EquipmentCreateNfcDto equipmentDto = objectMapper.treeToValue(data, EquipmentCreateNfcDto.class);
                    return equipmentService.createFromNfc(equipmentDto);

                case ACCESS_CONTROL_REGISTRATION:
                    AccessControlCreateNfcDto acDto = objectMapper.treeToValue(data, AccessControlCreateNfcDto.class);
                    return accessControlService.createFromNfc(acDto);

                default:
                    throw new IllegalArgumentException("不支持的NFC录入类型: " + request.getType());
            }
        } catch (Exception e) {
            log.error("处理NFC录入时发生错误", e);
            throw new RuntimeException("处理NFC录入失败: " + e.getMessage(), e);
        }
    }
}