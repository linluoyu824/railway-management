package com.railway.management.nfc.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NfcEntryRequest {

    @NotNull(message = "NFC数据类型不能为空")
    private NfcEntryType type;

    @NotNull(message = "NFC数据内容不能为空")
    private JsonNode data; // 使用JsonNode来接收灵活的JSON对象
}