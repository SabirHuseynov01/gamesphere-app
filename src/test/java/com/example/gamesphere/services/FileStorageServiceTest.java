package com.example.gamesphere.services;

import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    private final FileStorageService fileStorageService = new FileStorageService(
            System.getProperty("java.io.tmpdir") + "/gamesphere-test-uploads"
    );

    @Test
    void emptyFileIsRejectedBeforeFilesystemAccess() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "cover.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> fileStorageService.storeProductImage(5L, emptyFile))
                .isInstanceOf(BusinessException.class)
                .hasMessage("File cannot be empty.");
    }

    @Test
    void nullFileIsRejected() {
        assertThatThrownBy(() -> fileStorageService.storeProductImage(5L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("File cannot be empty.");
    }
}
