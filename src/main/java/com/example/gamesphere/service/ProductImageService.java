package com.example.gamesphere.service;

import com.example.gamesphere.dto.response.ProductImageResponse;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.ProductImage;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.repository.ProductImageRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    @Transactional
    public ProductImageResponse uploadProductImage(Long productId, MultipartFile file, boolean primary, int sortOrder) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        User user = getCurrentUser();
        if (product.getSeller() == null || !product.getSeller().getId().equals(user.getId())) {
            throw new BusinessException("You can only upload images for your own products.");
        }

        String imageUrl = fileStorageService.storeProductImage(productId, file);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(imageUrl);
        image.setPrimary(primary);
        image.setSortOrder(sortOrder);

        if (primary) {
            product.setImageUrl(imageUrl);
            productRepository.save(product);
        }

        return toResponse(productImageRepository.save(image));
    }

    public List<ProductImageResponse> getProductImages(Long productId) {
        return productImageRepository.findByProductIdOrderBySortOrderAsc(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductImageResponse toResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getProduct().getId(),
                image.getImageUrl(),
                image.getSortOrder(),
                image.isPrimary());
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
