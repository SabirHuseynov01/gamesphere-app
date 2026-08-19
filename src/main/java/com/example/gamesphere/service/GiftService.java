package com.example.gamesphere.service;

import com.example.gamesphere.dto.response.GiftResponse;
import com.example.gamesphere.entity.Gift;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.OrderItem;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.GiftStatus;
import com.example.gamesphere.enums.NotificationType;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.GiftMapper;
import com.example.gamesphere.repository.GiftRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GiftService {

    private final GiftRepository giftRepository;
    private final UserRepository userRepository;
    private final GiftMapper giftMapper;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final EntitlementService entitlementService;

    @Transactional
    public void createGiftsForPaidOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            if (!item.isGift()) {
                continue;
            }

            for (int i = 0; i < item.getQuantity(); i++) {
                Gift gift = Gift.builder()
                        .sender(order.getUser())
                        .recipientEmail(item.getGiftRecipientEmail())
                        .product(item.getProduct())
                        .orderItem(item)
                        .message(item.getGiftMessage())
                        .claimToken(generateToken())
                        .status(GiftStatus.PENDING)
                        .expiresAt(LocalDateTime.now().plusDays(30))
                        .build();

                Gift savedGift = giftRepository.save(gift);
                emailService.sendGiftReceivedEmail(savedGift);

                userRepository.findByEmail(item.getGiftRecipientEmail())
                        .ifPresent(recipient -> notificationService.createNotification(
                                recipient,
                                NotificationType.GIFT_RECEIVED,
                                "New gift received",
                                order.getUser().getUsername() + " sent you " + item.getProduct().getName()
                        ));
            }
        }
    }

    public List<GiftResponse> getSentGifts() {
        User user = getCurrentUser();
        return giftRepository.findBySenderIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(giftMapper::toResponse)
                .toList();
    }

    public List<GiftResponse> getReceivedGifts() {
        User user = getCurrentUser();
        return giftRepository.findByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(user.getEmail())
                .stream()
                .map(giftMapper::toResponse)
                .toList();
    }

    @Transactional
    public GiftResponse claimGift(String claimToken) {
        User user = getCurrentUser();

        Gift gift = giftRepository.findByClaimToken(claimToken)
                .orElseThrow(() -> new ResourceNotFoundException("Gift not found"));

        if (!gift.getRecipientEmail().equalsIgnoreCase(user.getEmail())) {
            throw new BusinessException("This gift belongs to another recipient.");
        }

        if (gift.getStatus() != GiftStatus.PENDING) {
            throw new BusinessException("Gift is not claimable.");
        }

        if (gift.getExpiresAt() != null && gift.getExpiresAt().isBefore(LocalDateTime.now())) {
            gift.setStatus(GiftStatus.EXPIRED);
            throw new BusinessException("Gift is expired.");
        }

        gift.setRecipientUser(user);
        entitlementService.grantGift(user, gift);
        gift.setStatus(GiftStatus.CLAIMED);
        gift.setClaimedAt(LocalDateTime.now());

        notificationService.createNotification(
                gift.getSender(),
                NotificationType.GIFT_CLAIMED,
                "Gift claimed",
                user.getUsername() + " claimed your gift: " + gift.getProduct().getName()
        );

        return giftMapper.toResponse(giftRepository.save(gift));
    }

    private String generateToken() {
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (giftRepository.existsByClaimToken(token));
        return token;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

