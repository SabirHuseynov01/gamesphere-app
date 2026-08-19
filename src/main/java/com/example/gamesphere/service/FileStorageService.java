package com.example.gamesphere.service;

import com.example.gamesphere.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadRoot;

    public FileStorageService(
            @Value("${app.storage.upload-dir:${user.home}/gamesphere-uploads}") String uploadDirectory) {
        this.uploadRoot = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    public String storeProductImage(Long productId, MultipartFile file) {
        return storeImage("products", productId, file);
    }

    public String storeGameCover(Long gameId, MultipartFile file) {
        return storeImage("games", gameId, file);
    }

    private String storeImage(String resourceDirectory, Long resourceId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File cannot be empty.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null
                ? "image"
                : file.getOriginalFilename());
        String extension = "";
        int extensionIndex = originalFilename.lastIndexOf('.');
        if (extensionIndex >= 0) {
            extension = originalFilename.substring(extensionIndex);
        }

        String filename = UUID.randomUUID() + extension;
        Path targetDirectory = uploadRoot.resolve(resourceDirectory).resolve(String.valueOf(resourceId));
        Path target = targetDirectory.resolve(filename).normalize();

        try {
            Files.createDirectories(targetDirectory);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException("Could not store file.");
        }

        return "/uploads/" + resourceDirectory + "/" + resourceId + "/" + filename;
    }
}