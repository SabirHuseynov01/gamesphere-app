package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.GameCreateRequest;
import com.example.gamesphere.dto.request.GameOfferCriteria;
import com.example.gamesphere.dto.response.GameComparisonResponse;
import com.example.gamesphere.dto.response.GameResponse;
import com.example.gamesphere.entity.Game;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.enums.Platform;
import com.example.gamesphere.enums.ProductStatus;
import com.example.gamesphere.enums.ProductType;
import com.example.gamesphere.repository.GameRepository;
import com.example.gamesphere.service.FileStorageService;
import com.example.gamesphere.service.GameQueryService;
import com.example.gamesphere.service.GameService;
import com.example.gamesphere.util.DiscountCalculator;
import com.example.gamesphere.util.SlugGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest extends ServiceTestSupport {

    @Mock GameRepository gameRepository;
    @Mock GameQueryService gameQueryService;
    @Mock SlugGenerator slugGenerator;
    @Mock DiscountCalculator discountCalculator;
    @Mock FileStorageService fileStorageService;
    @InjectMocks GameService gameService;

    @Test
    void createGameAddsNumericSuffixWhenSlugAlreadyExists() {
        GameCreateRequest request = new GameCreateRequest();
        request.setTitle("Need for Speed Unbound");
        when(slugGenerator.generate(request.getTitle())).thenReturn("need-for-speed-unbound");
        when(gameRepository.existsBySlug("need-for-speed-unbound")).thenReturn(true);
        when(gameRepository.existsBySlug("need-for-speed-unbound-2")).thenReturn(false);
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game game = invocation.getArgument(0);
            game.setId(11L);
            return game;
        });

        GameResponse response = gameService.createGame(request);

        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getSlug()).isEqualTo("need-for-speed-unbound-2");
    }

    @Test
    void compareGameOffersCalculatesLowestPriceAndAvailablePlatforms() {
        Game game = withId(Game.builder().title("Battlefield 6").slug("battlefield-6").build(), 1L);
        Product pc = offer(10L, Platform.PC, "59.99");
        Product xbox = offer(11L, Platform.XBOX, "69.99");
        GameOfferCriteria criteria = new GameOfferCriteria();
        when(gameRepository.findBySlugAndIsDeletedFalse("battlefield-6")).thenReturn(Optional.of(game));
        when(gameQueryService.findOffers(1L, criteria)).thenReturn(List.of(pc, xbox));
        when(discountCalculator.finalPrice(pc.getPrice(), pc.getDiscountPrice()))
                .thenReturn(new BigDecimal("49.99"));
        when(discountCalculator.finalPrice(xbox.getPrice(), xbox.getDiscountPrice()))
                .thenReturn(new BigDecimal("69.99"));

        GameComparisonResponse response = gameService.compareGameOffers("battlefield-6", criteria);

        assertThat(response.getOfferCount()).isEqualTo(2);
        assertThat(response.getLowestPrice()).isEqualByComparingTo("49.99");
        assertThat(response.getAvailablePlatforms()).containsExactlyInAnyOrder(Platform.PC, Platform.XBOX);
    }

    @Test
    void uploadCoverStoresFileAndUpdatesGame() {
        Game game = withId(Game.builder().title("Metro Exodus").slug("metro-exodus").build(), 8L);
        MockMultipartFile file = new MockMultipartFile(
                "file", "metro.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(gameRepository.findById(8L)).thenReturn(Optional.of(game));
        when(fileStorageService.storeGameCover(8L, file))
                .thenReturn("/uploads/games/8/cover.jpg");
        when(gameRepository.save(game)).thenReturn(game);

        GameResponse response = gameService.uploadCover(8L, file);

        assertThat(response.getCoverImageUrl()).isEqualTo("/uploads/games/8/cover.jpg");
    }

    private Product offer(Long id, Platform platform, String price) {
        Product product = new Product();
        product.setId(id);
        product.setName("Battlefield 6 " + platform);
        product.setSlug("battlefield-6-" + platform.name().toLowerCase());
        product.setPlatform(platform);
        product.setProductType(ProductType.GAME);
        product.setStatus(ProductStatus.ACTIVE);
        product.setPrice(new BigDecimal(price));
        product.setStockQuantity(10);
        return product;
    }
}
