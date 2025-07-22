package com.railway.managementsystem.equipment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parameter {
    /**
     * 参数名称, e.g., "额定电压"
     */
    private String name;
    /**
     * 参数值, e.g., "220"
     */
    private String value;
    /**
     * 参数单位, e.g., "V"
     */
    private String unit;
}