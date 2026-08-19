package com.example.gamesphere.services;

import com.example.gamesphere.dto.response.GlobalSearchResponse;
import com.example.gamesphere.entity.Game;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.enums.Platform;
import com.example.gamesphere.enums.ProductType;
import com.example.gamesphere.repository.GameRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.service.SearchService;
import com.example.gamesphere.util.DiscountCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest extends ServiceTestSupport {

    @Mock GameRepository gameRepository;
    @Mock ProductRepository productRepository;
    @Mock DiscountCalculator discountCalculator;
    @InjectMocks
    SearchService searchService;

    @Test
    void blankQueryReturnsEmptyResponseWithoutDatabaseCalls() {
        GlobalSearchResponse response = searchService.search("   ", 10);

        assertThat(response.getGames()).isEmpty();
        assertThat(response.getProducts()).isEmpty();
        verify(gameRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchCombinesGamesAndProductsAndCalculatesFinalPrice() {
        Game game = withId(Game.builder().title("PUBG Mobile").slug("pubg-mobile").build(), 1L);
        Product product = new Product();
        product.setId(2L);
        product.setName("660 UC");
        product.setSlug("660-uc-mobile");
        product.setGame(game);
        product.setPlatform(Platform.PC);
        product.setProductType(ProductType.IN_GAME_CURRENCY);
        product.setPrice(new BigDecimal("15.85"));

        when(gameRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(game)));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product)));
        when(discountCalculator.finalPrice(product.getPrice(), null)).thenReturn(product.getPrice());

        GlobalSearchResponse response = searchService.search(" pubg ", 100);

        assertThat(response.getQuery()).isEqualTo("pubg");
        assertThat(response.getGames()).hasSize(1);
        assertThat(response.getProducts()).hasSize(1);
        assertThat(response.getProducts().get(0).getFinalPrice()).isEqualByComparingTo("15.85");
    }
}

