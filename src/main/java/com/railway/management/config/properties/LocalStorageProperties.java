package com.railway.management.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "storage.local")
public class LocalStorageProperties {

    /**
     * 文件上传的物理存储路径
     */
    private String uploadPath;

    /**
     * 文件的外部访问路径 (URL前缀)
     */
    private String accessPath;
}