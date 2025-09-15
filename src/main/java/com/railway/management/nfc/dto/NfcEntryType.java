package com.railway.management.nfc.dto;

/**
 * NFC 统一录入类型枚举
 */
public enum NfcEntryType {
    /** 工具巡检/借还 */
    TOOL,
    /** 设备巡检 */
    EQUIPMENT,
    /** 门禁进入 */
    ACCESS_CONTROL_IN,
    /** 门禁离开 */
    ACCESS_CONTROL_OUT,
    /** 新工具注册 */
    TOOL_REGISTRATION,
    /** 新设备注册 */
    EQUIPMENT_REGISTRATION,
    /** 新门禁注册 */
    ACCESS_CONTROL_REGISTRATION
}