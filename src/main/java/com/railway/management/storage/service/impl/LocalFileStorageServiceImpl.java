package com.railway.management.storage.service.impl;

import com.railway.management.config.properties.LocalStorageProperties;
import com.railway.management.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final LocalStorageProperties properties;

    @Override
    public String upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();

        // 1. 生成唯一文件名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString() + extension;

        // 2. 确定文件存储的完整物理路径
        Path uploadPath = Paths.get(properties.getUploadPath());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(newFileName);

        // 3. 将文件保存到磁盘
        Files.copy(file.getInputStream(), filePath);

        // 4. 构建可访问的URL并返回
        // 例如: /uploads/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.jpg
        // 注意：accessPath需要以'/'结尾，或者在这里处理
        String accessPath = properties.getAccessPath();
        // 确保 accessPath 以 / 结尾，但不是 //
        String url = (accessPath.endsWith("/") ? accessPath : accessPath + "/") + newFileName;
        return url;
    }
}