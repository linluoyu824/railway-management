package com.railway.management.nfc.service;

import com.railway.management.nfc.dto.NfcEntryRequest;

public interface NfcEntryService {
    /**
     * 处理NFC统一录入请求
     * @param request 包含类型和数据的请求体
     * @return 返回创建的实体或处理结果
     */
    Object processNfcEntry(NfcEntryRequest request);
}