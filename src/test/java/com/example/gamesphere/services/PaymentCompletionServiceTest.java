package com.example.gamesphere.services;

import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.Payment;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.enums.PaymentMethod;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.PaymentRepository;
import com.example.gamesphere.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCompletionServiceTest extends ServiceTestSupport {

    @Mock PaymentRepository paymentRepository;
    @Mock OrderRepository orderRepository;
    @Mock GiftService giftService;
    @Mock EntitlementService entitlementService;
    @Mock TopUpFulfillmentService topUpFulfillmentService;
    @Mock NotificationService notificationService;
    @Mock EmailService emailService;
    private PaymentCompletionService paymentCompletionService;

    @BeforeEach
    void setUp() {
        paymentCompletionService = new PaymentCompletionService(
                paymentRepository,
                orderRepository,
                giftService,
                entitlementService,
                topUpFulfillmentService,
                notificationService,
                emailService);
    }

    @Test
    void completePersistsPaymentAndRunsEveryDeliveryChannelOnce() {
        User user = user(1L, "user@mail.com");
        Order order = Order.builder()
                .user(user)
                .orderNumber("GS-TEST")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("29.99"))
                .orderItems(new HashSet<>())
                .build();
        order.setId(7L);
        when(paymentRepository.findByOrderId(7L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentCompletionService.complete(
                order, user, PaymentMethod.STRIPE, null, "cs_test_123");

        assertThat(payment.getStatus().name()).isEqualTo("PAID");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getTransactionId()).isEqualTo("cs_test_123");
        verify(orderRepository).save(order);
        verify(giftService).createGiftsForPaidOrder(order);
        verify(entitlementService).grantForPaidOrder(order);
        verify(topUpFulfillmentService).createPendingForPaidOrder(order);
        verify(emailService).sendPaymentSuccessEmail(user, order, payment);
    }
}
