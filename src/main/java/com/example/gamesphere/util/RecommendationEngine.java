package com.example.gamesphere.util;

import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.Wishlist;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendationEngine {

    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;

    public List<Product> recommendSameGenre(Product product) {
        if (product.getGenre() == null || product.getGenre().isBlank()) {
            return Collections.emptyList();
        }
        return productRepository.findTop10ByGenreAndIdNotAndIsActiveTrue(product.getGenre(), product.getId());
    }

    public List<Product> recommendTopReviewed() {
        return productRepository.findTopReviewedProducts().stream()
                .limit(10)
                .toList();
    }

    public List<Product> recommendFromWishlistCategories(Long userId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElse(null);

        if (wishlist == null || wishlist.getProducts().isEmpty()) {
            return recommendTopReviewed();
        }

        List<Long> categoryIds = wishlist.getProducts().stream()
                .flatMap(product -> product.getCategories().stream())
                .map(category -> category.getId())
                .distinct()
                .toList();

        List<Long> excludedProductIds = wishlist.getProducts().stream()
                .map(Product::getId)
                .toList();

        if (categoryIds.isEmpty() || excludedProductIds.isEmpty()) {
            return recommendTopReviewed();
        }

        return productRepository.findRecommendedByCategoryIds(categoryIds, excludedProductIds).stream()
                .limit(10)
                .toList();
    }
}
