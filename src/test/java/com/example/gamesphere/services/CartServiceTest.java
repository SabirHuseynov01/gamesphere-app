package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.AddCartItemRequest;
import com.example.gamesphere.dto.response.CartResponse;
import com.example.gamesphere.entity.Cart;
import com.example.gamesphere.entity.CartItem;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.mapper.CartMapper;
import com.example.gamesphere.repository.CartRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest extends ServiceTestSupport {

    @Mock CartRepository cartRepository;
    @Mock UserRepository userRepository;
    @Mock ProductRepository productRepository;
    @Mock CartMapper cartMapper;
    @InjectMocks
    CartService cartService;

    @Test
    void addToCartCreatesItemWithCurrentPrice() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Product product = product(10L, true, 5);
        product.setDiscountPrice(new BigDecimal("8.00"));
        Cart cart = new Cart();
        cart.setUser(user);
        CartResponse expected = new CartResponse();

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(expected);

        assertThat(cartService.addToCart(new AddCartItemRequest(10L, 2))).isSameAs(expected);
        assertThat(cart.getCartItems()).singleElement().satisfies(item -> {
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getPriceAtAddTime()).isEqualByComparingTo("8.00");
        });
    }

    @Test
    void inactiveProductCannotBeAddedToCart() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product(10L, false, 5)));

        assertThatThrownBy(() -> cartService.addToCart(new AddCartItemRequest(10L, 1)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Product is not available.");
    }

    @Test
    void redirectOfferCannotBeAddedToCart() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Product product = product(10L, true, 5);
        product.setDeliveryType(DeliveryType.STORE_REDIRECT);
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addToCart(new AddCartItemRequest(10L, 1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("external store");
    }

    @Test
    void topUpProductIsAddedBeforeThePlayerAccountIdIsKnown() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Product product = product(10L, true, 5);
        product.setDeliveryType(DeliveryType.PLAYER_ID_TOP_UP);
        product.setRequiresPlayerId(true);
        product.setPlayerIdLabel("PUBG Mobile Player ID");
        Cart cart = new Cart();
        cart.setUser(user);
        CartResponse expected = new CartResponse();

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(expected);

        assertThat(cartService.addToCart(new AddCartItemRequest(10L, 1))).isSameAs(expected);
        assertThat(cart.getCartItems()).singleElement().satisfies(item ->
                assertThat(item.getPlayerAccountId()).isNull());
    }

    @Test
    void existingRowAdoptsThePlayerAccountIdSuppliedLater() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Product product = product(10L, true, 5);
        product.setDeliveryType(DeliveryType.PLAYER_ID_TOP_UP);
        product.setRequiresPlayerId(true);
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setProduct(product);
        existing.setQuantity(1);
        cart.getCartItems().add(existing);

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(new CartResponse());

        cartService.addToCart(new AddCartItemRequest(10L, 1, "5606020504"));

        assertThat(existing.getPlayerAccountId()).isEqualTo("5606020504");
        assertThat(existing.getQuantity()).isEqualTo(2);
    }

    @Test
    void switchingAnExistingRowToAnotherPlayerAccountIsRefused() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Product product = product(10L, true, 5);
        product.setDeliveryType(DeliveryType.PLAYER_ID_TOP_UP);
        product.setRequiresPlayerId(true);
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setProduct(product);
        existing.setQuantity(1);
        existing.setPlayerAccountId("5606020504");
        cart.getCartItems().add(existing);

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.addToCart(new AddCartItemRequest(10L, 1, "9999999999")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void topUpProductStoresNormalizedPlayerAccountId() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Product product = product(10L, true, 5);
        product.setDeliveryType(DeliveryType.PLAYER_ID_TOP_UP);
        product.setRequiresPlayerId(true);
        product.setPlayerIdLabel("Riot ID");
        Cart cart = new Cart();
        cart.setUser(user);
        CartResponse expected = new CartResponse();

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(expected);

        AddCartItemRequest request = new AddCartItemRequest(10L, 1, "  Sabir#AZE  ");

        assertThat(cartService.addToCart(request)).isSameAs(expected);
        assertThat(cart.getCartItems()).singleElement().satisfies(item ->
                assertThat(item.getPlayerAccountId()).isEqualTo("Sabir#AZE"));
    }

    private Product product(Long id, boolean active, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName("Game key");
        product.setPrice(new BigDecimal("10.00"));
        product.setStockQuantity(stock);
        product.setActive(active);
        product.setDeliveryType(DeliveryType.DIGITAL_CODE);
        return product;
    }
}

