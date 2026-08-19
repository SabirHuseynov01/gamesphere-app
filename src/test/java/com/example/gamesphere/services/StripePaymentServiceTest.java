package com.example.gamesphere.services;

import com.example.gamesphere.config.StripeProperties;
import com.example.gamesphere.dto.request.CreateStripeCheckoutRequest;
import com.example.gamesphere.dto.response.StripeCheckoutResponse;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.enums.PaymentMethod;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.PaymentRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.PaymentCompletionService;
import com.example.gamesphere.service.StripeGateway;
import com.example.gamesphere.service.StripePaymentService;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripePaymentServiceTest extends ServiceTestSupport {

    @Mock
    StripeGateway stripeGateway;
    @Mock OrderRepository orderRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock UserRepository userRepository;
    @Mock
    PaymentCompletionService paymentCompletionService;
    private StripeProperties properties;
    private StripePaymentService stripePaymentService;

    @BeforeEach
    void setUp() {
        properties = new StripeProperties();
        properties.setEnabled(true);
        properties.setSecretKey("sk_test_example");
        properties.setWebhookSecret("whsec_example");
        properties.setCurrency("usd");
        stripePaymentService = new StripePaymentService(
                properties,
                stripeGateway,
                orderRepository,
                paymentRepository,
                userRepository,
                paymentCompletionService);
    }

    @Test
    void checkoutUsesCurrentUsersPendingOrder() throws Exception {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Order order = order(user);
        CreateStripeCheckoutRequest request = new CreateStripeCheckoutRequest();
        request.setOrderId(7L);
        Session session = new Session();
        session.setId("cs_test_123");
        session.setUrl("https://checkout.stripe.com/test");
        session.setExpiresAt(2_000_000_000L);
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(7L)).thenReturn(Optional.empty());
        when(stripeGateway.createCheckoutSession(order, user)).thenReturn(session);

        StripeCheckoutResponse response = stripePaymentService.createCheckout(request);

        assertThat(response.getSessionId()).isEqualTo("cs_test_123");
        assertThat(response.getCheckoutUrl()).contains("checkout.stripe.com");
    }

    @Test
    void completedSessionUsesLockedOrderAndSharedCompletion() {
        User user = user(1L, "user@mail.com");
        Order order = order(user);
        Session session = new Session();
        session.setId("cs_test_123");
        session.setAmountTotal(2999L);
        session.setCurrency("usd");
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("orderId", "7");
        metadata.put("userId", "1");
        session.setMetadata(metadata);
        when(paymentRepository.findByTransactionId("cs_test_123")).thenReturn(Optional.empty());
        when(orderRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(order));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        stripePaymentService.completeCheckout(session);

        verify(paymentCompletionService).complete(
                order, user, PaymentMethod.STRIPE, null, "cs_test_123");
    }

    private Order order(User user) {
        Order order = Order.builder()
                .user(user)
                .orderNumber("GS-STRIPE")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("29.99"))
                .orderItems(new HashSet<>())
                .build();
        order.setId(7L);
        return order;
    }
}
