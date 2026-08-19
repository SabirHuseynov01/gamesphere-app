package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.ProductFilterRequest;
import com.example.gamesphere.dto.response.PageResponse;
import com.example.gamesphere.dto.response.ProductResponse;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.ProductMapper;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.specification.ProductSpecification;
import com.example.gamesphere.util.RecommendationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final RecommendationEngine recommendationEngine;
    private final UserRepository userRepository;

    public PageResponse<ProductResponse> getAllProducts(ProductFilterRequest filter, Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(
                ProductSpecification.withFilter(filter), pageable);

        return PageResponse.of(productPage.map(productMapper::toResponse));
    }

    public ProductResponse getProductById(Long id) {
        var product = productRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found ID: " + id));
        return productMapper.toResponse(product);
    }

    public ProductResponse getProductBySlug(String slug) {
        var product = productRepository.findBySlugAndIsActiveTrueAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found slug: " + slug));
        return productMapper.toResponse(product);
    }

    public List<ProductResponse> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerIdAndIsActiveTrueAndIsDeletedFalse(sellerId).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Cacheable("activeProducts")
    public List<ProductResponse> getActiveProducts() {
        return productRepository.findByIsActiveTrue().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "productRecommendations", key = "'sameGenre:' + #productId")
    public List<ProductResponse> getSameGenreRecommendations(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return recommendationEngine.recommendSameGenre(product).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "productRecommendations", key = "'topReviewed'")
    public List<ProductResponse> getTopReviewedRecommendations() {
        return recommendationEngine.recommendTopReviewed().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "productRecommendations", key = "'wishlist:' + #userId")
    public List<ProductResponse> getWishlistCategoryRecommendations(Long userId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
        if (!currentUserId.equals(userId)) {
            throw new ResourceNotFoundException("Wishlist not found");
        }
        return recommendationEngine.recommendFromWishlistCategories(userId).stream()
                .map(productMapper::toResponse)
                .toList();
    }
}
