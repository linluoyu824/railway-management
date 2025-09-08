package com.railway.management.equipment.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 作业指导书实体
 */
@Data
@TableName("work_instruction")
public class WorkInstruction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String url;
}