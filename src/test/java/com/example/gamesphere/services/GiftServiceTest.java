package com.example.gamesphere.services;

import com.example.gamesphere.dto.response.GiftResponse;
import com.example.gamesphere.entity.Gift;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.OrderItem;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.GiftStatus;
import com.example.gamesphere.mapper.GiftMapper;
import com.example.gamesphere.repository.GiftRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.EmailService;
import com.example.gamesphere.service.EntitlementService;
import com.example.gamesphere.service.GiftService;
import com.example.gamesphere.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GiftServiceTest extends ServiceTestSupport {

    @Mock GiftRepository giftRepository;
    @Mock UserRepository userRepository;
    @Mock GiftMapper giftMapper;
    @Mock NotificationService notificationService;
    @Mock EmailService emailService;
    @Mock EntitlementService entitlementService;
    private GiftService giftService;

    @BeforeEach
    void setUp() {
        giftService = new GiftService(
                giftRepository,
                userRepository,
                giftMapper,
                notificationService,
                emailService,
                entitlementService);
    }

    @Test
    void intendedRecipientCanClaimPendingGift() {
        authenticate("friend@mail.com");
        User recipient = user(2L, "friend@mail.com");
        User sender = user(1L, "sender@mail.com");
        Product product = withId(new Product(), 9L);
        product.setName("Mortal Kombat 1");
        Gift gift = Gift.builder()
                .sender(sender)
                .recipientEmail("FRIEND@mail.com")
                .product(product)
                .claimToken("claim-token")
                .status(GiftStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        GiftResponse expected = new GiftResponse();

        when(userRepository.findByEmail("friend@mail.com")).thenReturn(Optional.of(recipient));
        when(giftRepository.findByClaimToken("claim-token")).thenReturn(Optional.of(gift));
        when(giftRepository.save(gift)).thenReturn(gift);
        when(giftMapper.toResponse(gift)).thenReturn(expected);

        assertThat(giftService.claimGift("claim-token")).isSameAs(expected);
        assertThat(gift.getStatus()).isEqualTo(GiftStatus.CLAIMED);
        assertThat(gift.getRecipientUser()).isSameAs(recipient);
        assertThat(gift.getClaimedAt()).isNotNull();
        verify(entitlementService).grantGift(recipient, gift);
        verify(notificationService).createNotification(
                org.mockito.ArgumentMatchers.eq(sender),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq("Gift claimed"),
                org.mockito.ArgumentMatchers.contains("Mortal Kombat 1"));
    }

    @Test
    void paidGiftOrderCreatesGiftAndSendsEmail() {
        User sender = user(1L, "sender@mail.com");
        Product product = withId(new Product(), 9L);
        product.setName("Mortal Kombat 1");
        Order order = new Order();
        order.setUser(sender);
        order.setOrderItems(new HashSet<>());
        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(1)
                .gift(true)
                .giftRecipientEmail("friend@mail.com")
                .build();
        order.getOrderItems().add(item);
        when(giftRepository.save(any(Gift.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        giftService.createGiftsForPaidOrder(order);

        verify(emailService).sendGiftReceivedEmail(any(Gift.class));
    }
}


