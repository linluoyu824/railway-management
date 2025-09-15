package com.railway.management.common.train.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("train")
public class Train {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String trainNumber; // 列车号

    private String model; // 型号

    private TrainStatus status; // 状态

    private Long departmentId; // 所属部门ID

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}