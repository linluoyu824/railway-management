package com.railway.management.storage.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    /**
     * Uploads a file and returns its public URL.
     *
     * @param file the file to upload
     * @return the URL of the uploaded file
     */
    String upload(MultipartFile file) throws IOException;
}