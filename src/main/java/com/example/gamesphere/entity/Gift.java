package com.example.gamesphere.entity;

import com.example.gamesphere.enums.GiftStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gifts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gift extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id")
    private User recipientUser;

    @Column(nullable = false)
    private String recipientEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(length = 500)
    private String message;

    @Column(nullable = false, unique = true)
    private String claimToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GiftStatus status = GiftStatus.PENDING;

    private LocalDateTime claimedAt;
    private LocalDateTime expiresAt;
}
