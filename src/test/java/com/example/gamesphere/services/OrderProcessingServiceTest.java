package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.CreateOrderItemRequest;
import com.example.gamesphere.dto.request.CreateOrderRequest;
import com.example.gamesphere.entity.Cart;
import com.example.gamesphere.entity.CartItem;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.repository.CartRepository;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.service.OrderProcessingService;
import com.example.gamesphere.util.DiscountCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderProcessingServiceTest extends ServiceTestSupport {

    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock CartRepository cartRepository;
    @Mock DiscountCalculator discountCalculator;
    @InjectMocks
    OrderProcessingService orderProcessingService;

    @Test
    void processOrderCreatesPendingOrderCalculatesTotalAndReducesStock() {
        User user = user(1L, "user@mail.com");
        Product product = new Product();
        product.setId(3L);
        product.setName("Baldur's Gate 3");
        product.setPrice(new BigDecimal("59.99"));
        product.setStockQuantity(10);
        product.setActive(true);
        product.setDeliveryType(DeliveryType.DIGITAL_CODE);
        CreateOrderItemRequest item = new CreateOrderItemRequest();
        item.setProductId(3L);
        item.setQuantity(2);
        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(item));

        when(productRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(product));
        when(discountCalculator.finalPrice(product.getPrice(), null)).thenReturn(new BigDecimal("49.99"));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderProcessingService.processOrder(user, request);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("99.98");
        assertThat(order.getOrderItems()).singleElement().satisfies(orderItem ->
                assertThat(orderItem.getQuantity()).isEqualTo(2));
        assertThat(product.getStockQuantity()).isEqualTo(8);
    }

    @Test
    void processCartOrderCopiesPlayerAccountIdAndClearsCart() {
        User user = user(1L, "user@mail.com");
        Product product = new Product();
        product.setId(6L);
        product.setName("PUBG Mobile 8100 UC");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(10);
        product.setActive(true);
        product.setDeliveryType(DeliveryType.PLAYER_ID_TOP_UP);
        product.setRequiresPlayerId(true);
        product.setPlayerIdLabel("PUBG Mobile Player ID");

        Cart cart = new Cart();
        cart.setUser(user);
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setPlayerAccountId("5123456789");
        cart.getCartItems().add(cartItem);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdForUpdate(6L)).thenReturn(Optional.of(product));
        when(discountCalculator.finalPrice(product.getPrice(), null))
                .thenReturn(new BigDecimal("99.99"));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(cart)).thenReturn(cart);

        Order order = orderProcessingService.processCartOrder(user);

        assertThat(order.getOrderItems()).singleElement().satisfies(orderItem -> {
            assertThat(orderItem.getProduct().getId()).isEqualTo(6L);
            assertThat(orderItem.getPlayerAccountId()).isEqualTo("5123456789");
        });
        assertThat(cart.getCartItems()).isEmpty();
        assertThat(product.getStockQuantity()).isEqualTo(9);
    }
}


