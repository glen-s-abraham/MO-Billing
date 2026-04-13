package com.mariasorganics.billing.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:./uploads/}")
    private String uploadDirRoot;

    public String storeLogoFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDirRoot, "logos");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            if (file.isEmpty()) {
                return null;
            }
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            return "/uploads/logos/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
    }

    public void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) return;
        try {
            String filename = relativePath.replace("/uploads/logos/", "");
            Path filePath = Paths.get(uploadDirRoot, "logos", filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + e.getMessage());
        }
    }
}
