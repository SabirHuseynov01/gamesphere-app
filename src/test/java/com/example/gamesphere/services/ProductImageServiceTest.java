package com.example.gamesphere.services;

import com.example.gamesphere.dto.response.ProductImageResponse;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.ProductImage;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.repository.ProductImageRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.FileStorageService;
import com.example.gamesphere.service.ProductImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest extends ServiceTestSupport {

    @Mock ProductRepository productRepository;
    @Mock ProductImageRepository productImageRepository;
    @Mock
    FileStorageService fileStorageService;
    @Mock UserRepository userRepository;
    @InjectMocks
    ProductImageService productImageService;

    @Test
    void primaryUploadUpdatesProductCoverAndPersistsImage() {
        authenticate("seller@mail.com");
        User seller = user(2L, "seller@mail.com");
        Product product = withId(new Product(), 5L);
        product.setSeller(seller);
        MultipartFile file = mock(MultipartFile.class);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("seller@mail.com")).thenReturn(Optional.of(seller));
        when(fileStorageService.storeProductImage(5L, file))
                .thenReturn("/uploads/products/5/cover.png");
        when(productImageRepository.save(org.mockito.ArgumentMatchers.any(ProductImage.class)))
                .thenAnswer(invocation -> {
                    ProductImage image = invocation.getArgument(0);
                    image.setId(9L);
                    return image;
                });

        ProductImageResponse response = productImageService.uploadProductImage(5L, file, true, 0);

        assertThat(product.getImageUrl()).isEqualTo("/uploads/products/5/cover.png");
        assertThat(response.getId()).isEqualTo(9L);
        assertThat(response.isPrimary()).isTrue();
        verify(productRepository).save(product);
    }
}
