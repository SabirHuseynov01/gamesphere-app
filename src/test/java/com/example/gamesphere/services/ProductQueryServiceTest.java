package com.example.gamesphere.services;

import com.example.gamesphere.dto.response.ProductResponse;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.mapper.ProductMapper;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.ProductQueryService;
import com.example.gamesphere.util.RecommendationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductQueryServiceTest extends ServiceTestSupport {

    @Mock ProductRepository productRepository;
    @Mock ProductMapper productMapper;
    @Mock RecommendationEngine recommendationEngine;
    @Mock UserRepository userRepository;
    private ProductQueryService productQueryService;

    @BeforeEach
    void setUp() {
        productQueryService = new ProductQueryService(
                productRepository,
                productMapper,
                recommendationEngine,
                userRepository);
    }

    @Test
    void getProductByIdMapsRepositoryEntity() {
        Product product = new Product();
        ProductResponse expected = new ProductResponse();
        when(productRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(4L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(expected);

        assertThat(productQueryService.getProductById(4L)).isSameAs(expected);
    }

    @Test
    void sameGenreRecommendationsDelegateToRecommendationEngine() {
        Product source = new Product();
        Product recommendation = new Product();
        ProductResponse expected = new ProductResponse();
        when(productRepository.findById(4L)).thenReturn(Optional.of(source));
        when(recommendationEngine.recommendSameGenre(source)).thenReturn(List.of(recommendation));
        when(productMapper.toResponse(recommendation)).thenReturn(expected);

        assertThat(productQueryService.getSameGenreRecommendations(4L)).containsExactly(expected);
    }

    @Test
    void wishlistRecommendationsRequireCurrentUserOwnership() {
        authenticate("user@mail.com");
        User user = user(7L, "user@mail.com");
        Product recommendation = new Product();
        ProductResponse expected = new ProductResponse();
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(recommendationEngine.recommendFromWishlistCategories(7L))
                .thenReturn(List.of(recommendation));
        when(productMapper.toResponse(recommendation)).thenReturn(expected);

        assertThat(productQueryService.getWishlistCategoryRecommendations(7L))
                .containsExactly(expected);
    }
}