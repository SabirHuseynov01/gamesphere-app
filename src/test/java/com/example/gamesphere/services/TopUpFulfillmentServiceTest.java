package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.UpdateTopUpFulfillmentRequest;
import com.example.gamesphere.dto.response.TopUpFulfillmentResponse;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.OrderItem;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.TopUpFulfillment;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.enums.TopUpFulfillmentStatus;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.mapper.TopUpFulfillmentMapper;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.TopUpFulfillmentRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.NotificationService;
import com.example.gamesphere.service.TopUpFulfillmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopUpFulfillmentServiceTest extends ServiceTestSupport {

    @Mock TopUpFulfillmentRepository fulfillmentRepository;
    @Mock OrderRepository orderRepository;
    @Mock UserRepository userRepository;
    @Mock TopUpFulfillmentMapper fulfillmentMapper;
    @Mock NotificationService notificationService;
    private TopUpFulfillmentService fulfillmentService;

    @BeforeEach
    void setUp() {
        fulfillmentService = new TopUpFulfillmentService(
                fulfillmentRepository,
                orderRepository,
                userRepository,
                fulfillmentMapper,
                notificationService);
    }

    @Test
    void paidTopUpOrderCreatesPendingFulfillment() {
        OrderItem orderItem = topUpOrderItem(11L, "PLAYER-123");
        Order order = orderItem.getOrder();
        when(fulfillmentRepository.findByOrderItemId(11L)).thenReturn(Optional.empty());

        fulfillmentService.createPendingForPaidOrder(order);

        ArgumentCaptor<TopUpFulfillment> captor = ArgumentCaptor.forClass(TopUpFulfillment.class);
        verify(fulfillmentRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderItem()).isSameAs(orderItem);
        assertThat(captor.getValue().getStatus()).isEqualTo(TopUpFulfillmentStatus.PENDING);
        assertThat(captor.getValue().getAttemptCount()).isZero();
    }

    @Test
    void nonTopUpOrderItemDoesNotCreateFulfillment() {
        OrderItem orderItem = topUpOrderItem(11L, "PLAYER-123");
        orderItem.getProduct().setDeliveryType(DeliveryType.DIGITAL_CODE);

        fulfillmentService.createPendingForPaidOrder(orderItem.getOrder());

        verify(fulfillmentRepository, never()).save(any());
    }

    @Test
    void unpaidOrderCannotCreateFulfillment() {
        OrderItem orderItem = topUpOrderItem(11L, "PLAYER-123");
        orderItem.getOrder().setStatus(OrderStatus.PENDING);

        assertThatThrownBy(() -> fulfillmentService.createPendingForPaidOrder(orderItem.getOrder()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("paid order");
    }

    @Test
    void processingIncrementsAttemptCount() {
        TopUpFulfillment fulfillment = fulfillment(TopUpFulfillmentStatus.PENDING);
        TopUpFulfillmentResponse expected = new TopUpFulfillmentResponse();
        UpdateTopUpFulfillmentRequest request = new UpdateTopUpFulfillmentRequest(
                TopUpFulfillmentStatus.PROCESSING,
                null,
                null);
        when(fulfillmentRepository.findById(1L)).thenReturn(Optional.of(fulfillment));
        when(fulfillmentRepository.save(fulfillment)).thenReturn(fulfillment);
        when(fulfillmentMapper.toResponse(fulfillment)).thenReturn(expected);

        assertThat(fulfillmentService.updateStatus(1L, request)).isSameAs(expected);
        assertThat(fulfillment.getStatus()).isEqualTo(TopUpFulfillmentStatus.PROCESSING);
        assertThat(fulfillment.getAttemptCount()).isEqualTo(1);
        assertThat(fulfillment.getLastAttemptAt()).isNotNull();
    }

    @Test
    void completionRequiresProviderReference() {
        TopUpFulfillment fulfillment = fulfillment(TopUpFulfillmentStatus.PROCESSING);
        when(fulfillmentRepository.findById(1L)).thenReturn(Optional.of(fulfillment));
        UpdateTopUpFulfillmentRequest request = new UpdateTopUpFulfillmentRequest(
                TopUpFulfillmentStatus.COMPLETED,
                " ",
                null);

        assertThatThrownBy(() -> fulfillmentService.updateStatus(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Provider reference");
    }

    @Test
    void currentUserCanReadFulfillmentsForOwnedOrder() {
        authenticate("buyer@gamesphere.az");
        TopUpFulfillment fulfillment = fulfillment(TopUpFulfillmentStatus.PENDING);
        Order order = fulfillment.getOrderItem().getOrder();
        User user = order.getUser();
        TopUpFulfillmentResponse expected = new TopUpFulfillmentResponse();
        when(userRepository.findByEmail("buyer@gamesphere.az")).thenReturn(Optional.of(user));
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(fulfillmentRepository.findAllByOrderItemOrderIdOrderByIdAsc(7L))
                .thenReturn(List.of(fulfillment));
        when(fulfillmentMapper.toResponse(fulfillment)).thenReturn(expected);

        assertThat(fulfillmentService.getCurrentUserFulfillmentsByOrder(7L))
                .containsExactly(expected);
    }

    @Test
    void completingFulfillmentNotifiesBuyer() {
        TopUpFulfillment fulfillment = fulfillment(TopUpFulfillmentStatus.PROCESSING);
        TopUpFulfillmentResponse expected = new TopUpFulfillmentResponse();
        UpdateTopUpFulfillmentRequest request = new UpdateTopUpFulfillmentRequest(
                TopUpFulfillmentStatus.COMPLETED,
                "provider-ref-1",
                null);
        when(fulfillmentRepository.findById(1L)).thenReturn(Optional.of(fulfillment));
        when(fulfillmentRepository.save(fulfillment)).thenReturn(fulfillment);
        when(fulfillmentMapper.toResponse(fulfillment)).thenReturn(expected);

        assertThat(fulfillmentService.updateStatus(1L, request)).isSameAs(expected);
        verify(notificationService).createNotification(
                org.mockito.ArgumentMatchers.eq(fulfillment.getOrderItem().getOrder().getUser()),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq("Top-up completed"),
                org.mockito.ArgumentMatchers.contains("Demo top-up"));
    }

    private TopUpFulfillment fulfillment(TopUpFulfillmentStatus status) {
        TopUpFulfillment fulfillment = new TopUpFulfillment();
        fulfillment.setId(1L);
        fulfillment.setOrderItem(topUpOrderItem(11L, "PLAYER-123"));
        fulfillment.setStatus(status);
        return fulfillment;
    }

    private OrderItem topUpOrderItem(Long id, String playerAccountId) {
        User user = user(3L, "buyer@gamesphere.az");
        Order order = new Order();
        order.setId(7L);
        order.setUser(user);
        order.setOrderNumber("GS-TOPUP-7");
        order.setStatus(OrderStatus.PAID);
        order.setOrderItems(new HashSet<>());

        Product product = new Product();
        product.setId(9L);
        product.setName("Demo top-up");
        product.setDeliveryType(DeliveryType.PLAYER_ID_TOP_UP);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setPlayerAccountId(playerAccountId);
        order.getOrderItems().add(orderItem);
        return orderItem;
    }
}

