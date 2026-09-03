package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.ProductCreateRequest;
import com.example.gamesphere.dto.response.ProductResponse;
import com.example.gamesphere.entity.Game;
import com.example.gamesphere.entity.Category;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.SellerProfile;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.Platform;
import com.example.gamesphere.enums.ProductStatus;
import com.example.gamesphere.enums.ProductType;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.mapper.ProductMapper;
import com.example.gamesphere.repository.GameRepository;
import com.example.gamesphere.repository.CategoryRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.SellerProfileRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.ProductService;
import com.example.gamesphere.util.DiscountCalculator;
import com.example.gamesphere.util.SlugGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest extends ServiceTestSupport {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock GameRepository gameRepository;
    @Mock UserRepository userRepository;
    @Mock SellerProfileRepository sellerProfileRepository;
    @Mock ProductMapper productMapper;
    @Mock SlugGenerator slugGenerator;
    @Mock DiscountCalculator discountCalculator;
    @InjectMocks ProductService productService;

    @Test
    void approvedSellerCanCreateProductForExistingGame() {
        authenticate("seller@mail.com");
        User seller = user(2L, "seller@mail.com");
        seller.setSeller(true);
        SellerProfile profile = new SellerProfile();
        profile.setApproved(true);
        Game game = withId(Game.builder().title("Valorant").slug("valorant").build(), 7L);
        ProductCreateRequest request = request();
        request.setCategoryIds(Set.of(3L));
        Category category = withId(Category.builder().name("Shooter").build(), 3L);
        Product product = new Product();
        product.setName(request.getName());
        product.setPlatform(request.getPlatform());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        ProductResponse expected = new ProductResponse();

        when(userRepository.findByEmail("seller@mail.com")).thenReturn(Optional.of(seller));
        when(sellerProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(productMapper.toEntity(request)).thenReturn(product);
        when(gameRepository.findById(7L)).thenReturn(Optional.of(game));
        when(categoryRepository.findAllById(Set.of(3L))).thenReturn(List.of(category));
        when(slugGenerator.generate("2050 Valorant Points-PC")).thenReturn("2050-valorant-points-pc");
        when(productRepository.existsBySlug("2050-valorant-points-pc")).thenReturn(false);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(expected);

        assertThat(productService.createProduct(request)).isSameAs(expected);
        assertThat(product.getSeller()).isSameAs(seller);
        assertThat(product.getGame()).isSameAs(game);
        assertThat(product.getCategories()).containsExactly(category);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        verify(discountCalculator).validateDiscount(request.getPrice(), null);
    }

    @Test
    void ordinaryUserCannotCreateProductBeforeSellerApproval() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        SellerProfile pendingProfile = new SellerProfile();
        pendingProfile.setApproved(false);
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(sellerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(pendingProfile));

        assertThatThrownBy(() -> productService.createProduct(request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not been approved");
    }

    @Test
    void sameGameCanKeepDifferentEditionsButRejectsSameEditionOffer() {
        authenticate("seller@mail.com");
        User seller = user(2L, "seller@mail.com");
        seller.setSeller(true);
        SellerProfile profile = new SellerProfile();
        profile.setApproved(true);
        Game game = withId(Game.builder().title("Assassin's Creed Shadows").slug("assassins-creed-shadows").build(), 7L);

        ProductCreateRequest standardRequest = request();
        standardRequest.setName("Assassin's Creed Shadows Standard Edition");
        standardRequest.setEditionName("Standard Edition");
        standardRequest.setProductType(ProductType.GAME);
        standardRequest.setStoreName("Steam");

        Product standard = product("Assassin's Creed Shadows Standard Edition", "Standard Edition", game);
        when(userRepository.findByEmail("seller@mail.com")).thenReturn(Optional.of(seller));
        when(sellerProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(gameRepository.findById(7L)).thenReturn(Optional.of(game));
        when(productMapper.toEntity(standardRequest)).thenReturn(standard);
        when(productRepository.findByGameIdAndPlatformAndStoreNameIgnoreCaseAndEditionNameIgnoreCaseAndIsDeletedFalse(
                7L, Platform.PC, "Steam", "Standard Edition")).thenReturn(Optional.empty());
        when(slugGenerator.generate("Assassin's Creed Shadows Standard Edition-Standard Edition-PC"))
                .thenReturn("assassins-creed-shadows-standard-edition-pc");
        when(productRepository.existsBySlug("assassins-creed-shadows-standard-edition-pc")).thenReturn(false);
        when(productRepository.save(standard)).thenReturn(standard);
        when(productMapper.toResponse(standard)).thenReturn(new ProductResponse());

        productService.createProduct(standardRequest);

        ProductCreateRequest duplicateRequest = new ProductCreateRequest();
        duplicateRequest.setName("Assassin's Creed Shadows Standard Edition");
        duplicateRequest.setEditionName("Standard Edition");
        duplicateRequest.setGameId(7L);
        duplicateRequest.setPrice(new BigDecimal("59.99"));
        duplicateRequest.setStockQuantity(0);
        duplicateRequest.setPlatform(Platform.PC);
        duplicateRequest.setProductType(ProductType.GAME);
        duplicateRequest.setStoreName("Steam");
        duplicateRequest.setStoreUrl("https://store.steampowered.com/");
        Product duplicate = product("Assassin's Creed Shadows Standard Edition", "Standard Edition", game);
        when(productMapper.toEntity(duplicateRequest)).thenReturn(duplicate);
        when(productRepository.findByGameIdAndPlatformAndStoreNameIgnoreCaseAndEditionNameIgnoreCaseAndIsDeletedFalse(
                7L, Platform.PC, "Steam", "Standard Edition")).thenReturn(Optional.of(standard));

        assertThatThrownBy(() -> productService.createProduct(duplicateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("edition offer already exists");
        verify(productRepository, org.mockito.Mockito.times(1)).save(standard);
    }

    private Product product(String name, String editionName, Game game) {
        Product product = new Product();
        product.setName(name);
        product.setEditionName(editionName);
        product.setGame(game);
        product.setPlatform(Platform.PC);
        product.setStoreName("Steam");
        product.setStoreUrl("https://store.steampowered.com/");
        product.setPrice(new BigDecimal("59.99"));
        product.setStockQuantity(0);
        return product;
    }

    private ProductCreateRequest request() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("2050 Valorant Points");
        request.setGameId(7L);
        request.setPrice(new BigDecimal("19.99"));
        request.setStockQuantity(100);
        request.setPlatform(Platform.PC);
        request.setProductType(ProductType.IN_GAME_CURRENCY);
        request.setStoreName("Riot Games");
        request.setStoreUrl("https://example.test/valorant");
        return request;
    }
}
