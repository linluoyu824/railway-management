package com.railway.management.storage.service.impl;

import com.aliyun.oss.OSS;
import com.railway.management.config.properties.OssProperties;
import com.railway.management.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "oss", matchIfMissing = true)
public class OssFileStorageServiceImpl implements FileStorageService {

    private final OSS ossClient;
    private final OssProperties ossProperties;

    @Override
    public String upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();

        // Generate a unique object name to avoid collisions
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = "images/" + UUID.randomUUID().toString() + extension;

        // Use try-with-resources to ensure the input stream is closed
        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(ossProperties.getBucketName(), objectName, inputStream);
            log.info("文件已成功上传到OSS. Bucket: {}, ObjectName: {}", ossProperties.getBucketName(), objectName);
        }

        // Construct the public URL: https://<BucketName>.<Endpoint>/<ObjectName>
        return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + objectName;
    }
}