package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.CreateOrderRequest;
import com.example.gamesphere.dto.response.OrderResponse;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.event.OrderCreatedEvent;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.OrderMapper;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.OrderProcessingService;
import com.example.gamesphere.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest extends ServiceTestSupport {

    @Mock OrderRepository orderRepository;
    @Mock UserRepository userRepository;
    @Mock OrderMapper orderMapper;
    @Mock OrderProcessingService orderProcessingService;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks OrderService orderService;

    @Test
    void createOrderDelegatesProcessingPublishesEventAndMapsResponse() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        CreateOrderRequest request = new CreateOrderRequest(List.of(4L), null);
        Order order = new Order();
        OrderResponse expected = new OrderResponse();
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(orderProcessingService.processOrder(user, request)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        assertThat(orderService.createOrder(request)).isSameAs(expected);
        verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void createOrderRejectsEmptyProductSelectionBeforeProcessing() {
        CreateOrderRequest request = new CreateOrderRequest();

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("at least one product");
    }

    @Test
    void createOrderFromCartDelegatesProcessingPublishesEventAndMapsResponse() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Order order = new Order();
        OrderResponse expected = new OrderResponse();
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(orderProcessingService.processCartOrder(user)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        assertThat(orderService.createOrderFromCart()).isSameAs(expected);
        verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void getOrderByIdHidesAnotherUsersOrder() {
        authenticate("user@mail.com");
        User currentUser = user(1L, "user@mail.com");
        User owner = user(2L, "owner@mail.com");
        Order order = new Order();
        order.setId(9L);
        order.setUser(owner);
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(currentUser));
        when(orderRepository.findById(9L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById(9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found");
    }
}

