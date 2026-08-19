package com.example.gamesphere.service;


import com.example.gamesphere.config.StripeProperties;
import com.example.gamesphere.dto.request.CreateStripeCheckoutRequest;
import com.example.gamesphere.dto.response.StripeCheckoutResponse;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.enums.PaymentMethod;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.PaymentRepository;
import com.example.gamesphere.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StripePaymentService {

    private final StripeProperties stripeProperties;
    private final StripeGateway stripeGateway;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentCompletionService paymentCompletionService;

    @Transactional(readOnly = true)
    public StripeCheckoutResponse createCheckout(CreateStripeCheckoutRequest request) {
        requireCheckoutConfiguration();
        User user = getCurrentUser();
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Only PENDING orders can start Stripe Checkout.");
        }
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BusinessException("A payment already exists for this order.");
        }

        validateOrderCurrency(order);
        try {
            Session session = stripeGateway.createCheckoutSession(order, user);
            return new StripeCheckoutResponse(
                    session.getId(),
                    session.getUrl(),
                    session.getExpiresAt() == null ? null : Instant.ofEpochSecond(session.getExpiresAt()));
        } catch (StripeException ex) {
            throw new BusinessException("Stripe Checkout could not be created: " + ex.getMessage(),
                    HttpStatus.BAD_GATEWAY);
        }
    }

    public void handleWebhook(String payload, String signature) {
        requireWebhookConfiguration();
        final Event event;
        try {
            event = Webhook.constructEvent(payload, signature, stripeProperties.getWebhookSecret());
        } catch (Exception ex) {
            throw new BusinessException("Invalid Stripe webhook signature.");
        }

        if (!"checkout.session.completed".equals(event.getType())) {
            return;
        }

        StripeObject object = event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new BusinessException("Stripe webhook payload could not be deserialized."));
        if (!(object instanceof Session session)) {
            throw new BusinessException("Stripe webhook does not contain a checkout session.");
        }
        if (!"paid".equals(session.getPaymentStatus())) {
            return;
        }
        completeCheckout(session);
    }

    @Transactional
    public void completeCheckout(Session session) {
        Long orderId = parseId(session.getMetadata().get("orderId"), "orderId");
        Long metadataUserId = parseId(session.getMetadata().get("userId"), "userId");

        if (paymentRepository.findByTransactionId(session.getId()).isPresent()) {
            return;
        }

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        User user = userRepository.findByIdForUpdate(order.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getId().equals(metadataUserId)) {
            throw new BusinessException("Stripe checkout user does not match the order owner.");
        }
        verifyAmount(session, order);
        paymentCompletionService.complete(order, user, PaymentMethod.STRIPE, null, session.getId());
    }

    private void verifyAmount(Session session, Order order) {
        if (session.getAmountTotal() == null) {
            throw new BusinessException("Stripe checkout amount is missing.");
        }
        BigDecimal paid = BigDecimal.valueOf(session.getAmountTotal(), 2);
        if (paid.compareTo(order.getTotalAmount()) != 0) {
            throw new BusinessException("Stripe checkout amount does not match the order total.");
        }
        if (session.getCurrency() != null
                && !session.getCurrency().equalsIgnoreCase(stripeProperties.getCurrency())) {
            throw new BusinessException("Stripe checkout currency does not match the configured currency.");
        }
    }

    private void validateOrderCurrency(Order order) {
        boolean mismatch = order.getOrderItems().stream()
                .map(item -> item.getProduct().getCurrency())
                .filter(currency -> currency != null && !currency.isBlank())
                .anyMatch(currency -> !currency.equalsIgnoreCase(stripeProperties.getCurrency()));
        if (mismatch) {
            throw new BusinessException("All order items must use "
                    + stripeProperties.getCurrency().toUpperCase() + " for Stripe Checkout.");
        }
    }

    private void requireCheckoutConfiguration() {
        if (!stripeProperties.isEnabled() || isBlank(stripeProperties.getSecretKey())) {
            throw new BusinessException("Stripe is disabled or STRIPE_SECRET_KEY is not configured.");
        }
    }

    private void requireWebhookConfiguration() {
        if (!stripeProperties.isEnabled() || isBlank(stripeProperties.getWebhookSecret())) {
            throw new BusinessException("Stripe webhook is disabled or STRIPE_WEBHOOK_SECRET is not configured.");
        }
    }

    private Long parseId(String value, String field) {
        try {
            return Long.valueOf(value);
        } catch (RuntimeException ex) {
            throw new BusinessException("Stripe metadata is missing or invalid: " + field + ".");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
