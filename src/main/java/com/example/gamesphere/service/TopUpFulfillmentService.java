package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.UpdateTopUpFulfillmentRequest;
import com.example.gamesphere.dto.response.PageResponse;
import com.example.gamesphere.dto.response.TopUpFulfillmentResponse;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.OrderItem;
import com.example.gamesphere.entity.TopUpFulfillment;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.NotificationType;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.enums.TopUpFulfillmentStatus;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.TopUpFulfillmentMapper;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.TopUpFulfillmentRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TopUpFulfillmentService {

    private final TopUpFulfillmentRepository fulfillmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TopUpFulfillmentMapper fulfillmentMapper;
    private final NotificationService notificationService;

    @Transactional
    public void createPendingForPaidOrder(Order order) {
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("Top-up fulfillment can only be created for a paid order.");
        }

        order.getOrderItems().stream()
                .filter(this::isPlayerIdTopUp)
                .forEach(this::createPendingIfAbsent);
    }

    @Transactional(readOnly = true)
    public List<TopUpFulfillmentResponse> getCurrentUserFulfillmentsByOrder(Long orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }

        return fulfillmentRepository.findAllByOrderItemOrderIdOrderByIdAsc(orderId).stream()
                .map(fulfillmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TopUpFulfillmentResponse getCurrentUserFulfillment(Long id) {
        User user = getCurrentUser();
        TopUpFulfillment fulfillment = getFulfillment(id);

        if (!fulfillment.getOrderItem().getOrder().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Top-up fulfillment not found");
        }

        return fulfillmentMapper.toResponse(fulfillment);
    }

    @Transactional(readOnly = true)
    public PageResponse<TopUpFulfillmentResponse> getAll(
            TopUpFulfillmentStatus status,
            Pageable pageable) {
        Page<TopUpFulfillment> page = status == null
                ? fulfillmentRepository.findAll(pageable)
                : fulfillmentRepository.findAllByStatus(status, pageable);
        return PageResponse.of(page.map(fulfillmentMapper::toResponse));
    }

    @Transactional
    public TopUpFulfillmentResponse updateStatus(
            Long id,
            UpdateTopUpFulfillmentRequest request) {
        TopUpFulfillment fulfillment = getFulfillment(id);
        TopUpFulfillmentStatus nextStatus = request.getStatus();
        validateTransition(fulfillment.getStatus(), nextStatus);

        LocalDateTime now = LocalDateTime.now();
        switch (nextStatus) {
            case PROCESSING -> startProcessing(fulfillment, request, now);
            case COMPLETED -> complete(fulfillment, request, now);
            case FAILED -> fail(fulfillment, request, now);
            case CANCELLED -> cancel(fulfillment, request);
            case PENDING -> throw new BusinessException("A fulfillment cannot be moved back to PENDING.");
        }

        fulfillment.setStatus(nextStatus);
        TopUpFulfillment saved = fulfillmentRepository.save(fulfillment);
        notifyUser(saved);
        return fulfillmentMapper.toResponse(saved);
    }



    private boolean isPlayerIdTopUp(OrderItem orderItem) {
        return orderItem.getProduct().getDeliveryType() == DeliveryType.PLAYER_ID_TOP_UP;
    }

    private void createPendingIfAbsent(OrderItem orderItem) {
        if (orderItem.getPlayerAccountId() == null || orderItem.getPlayerAccountId().isBlank()) {
            throw new BusinessException("Player account ID is missing for top-up order item " + orderItem.getId() + ".");
        }

        if (fulfillmentRepository.findByOrderItemId(orderItem.getId()).isPresent()) {
            return;
        }

        TopUpFulfillment fulfillment = TopUpFulfillment.builder()
                .orderItem(orderItem)
                .status(TopUpFulfillmentStatus.PENDING)
                .attemptCount(0)
                .build();
        fulfillmentRepository.save(fulfillment);
    }

    private void startProcessing(
            TopUpFulfillment fulfillment,
            UpdateTopUpFulfillmentRequest request,
            LocalDateTime now) {
        fulfillment.setAttemptCount(fulfillment.getAttemptCount() + 1);
        fulfillment.setLastAttemptAt(now);
        fulfillment.setCompletedAt(null);
        fulfillment.setFailureReason(null);
        setProviderReferenceIfPresent(fulfillment, request.getProviderReference());
    }

    private void complete(
            TopUpFulfillment fulfillment,
            UpdateTopUpFulfillmentRequest request,
            LocalDateTime now) {
        String providerReference = normalize(request.getProviderReference());
        if (providerReference == null) {
            throw new BusinessException("Provider reference is required when completing a top-up.");
        }
        fulfillment.setProviderReference(providerReference);
        fulfillment.setFailureReason(null);
        fulfillment.setCompletedAt(now);
    }

    private void fail(
            TopUpFulfillment fulfillment,
            UpdateTopUpFulfillmentRequest request,
            LocalDateTime now) {
        String failureReason = normalize(request.getFailureReason());
        if (failureReason == null) {
            throw new BusinessException("Failure reason is required when a top-up fails.");
        }
        fulfillment.setFailureReason(failureReason);
        fulfillment.setLastAttemptAt(now);
        fulfillment.setCompletedAt(null);
        setProviderReferenceIfPresent(fulfillment, request.getProviderReference());
    }

    private void cancel(
            TopUpFulfillment fulfillment,
            UpdateTopUpFulfillmentRequest request) {
        fulfillment.setFailureReason(normalize(request.getFailureReason()));
        fulfillment.setCompletedAt(null);
        setProviderReferenceIfPresent(fulfillment, request.getProviderReference());
    }

    private void setProviderReferenceIfPresent(
            TopUpFulfillment fulfillment,
            String providerReference) {
        String normalized = normalize(providerReference);
        if (normalized != null) {
            fulfillment.setProviderReference(normalized);
        }
    }

    private void validateTransition(
            TopUpFulfillmentStatus current,
            TopUpFulfillmentStatus next) {
        Set<TopUpFulfillmentStatus> allowed = switch (current) {
            case PENDING -> Set.of(TopUpFulfillmentStatus.PROCESSING, TopUpFulfillmentStatus.CANCELLED);
            case PROCESSING -> Set.of(
                    TopUpFulfillmentStatus.COMPLETED,
                    TopUpFulfillmentStatus.FAILED,
                    TopUpFulfillmentStatus.CANCELLED);
            case FAILED -> Set.of(TopUpFulfillmentStatus.PROCESSING, TopUpFulfillmentStatus.CANCELLED);
            case COMPLETED, CANCELLED -> Set.of();
        };

        if (!allowed.contains(next)) {
            throw new BusinessException("Invalid top-up status transition: " + current + " -> " + next + ".");
        }
    }

    private void notifyUser(TopUpFulfillment fulfillment) {
        User user = fulfillment.getOrderItem().getOrder().getUser();
        String productName = fulfillment.getOrderItem().getProduct().getName();

        switch (fulfillment.getStatus()) {
            case COMPLETED -> notificationService.createNotification(
                    user,
                    NotificationType.TOP_UP_COMPLETED,
                    "Top-up completed",
                    productName + " was delivered to your player account.");
            case FAILED -> notificationService.createNotification(
                    user,
                    NotificationType.TOP_UP_FAILED,
                    "Top-up failed",
                    productName + " could not be delivered: " + fulfillment.getFailureReason());
            case CANCELLED -> notificationService.createNotification(
                    user,
                    NotificationType.TOP_UP_CANCELLED,
                    "Top-up cancelled",
                    productName + " fulfillment was cancelled.");
            default -> {
            }
        }
    }

    private TopUpFulfillment getFulfillment(Long id) {
        return fulfillmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Top-up fulfillment not found"));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}