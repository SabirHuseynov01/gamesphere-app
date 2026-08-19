package com.example.gamesphere.entity;

import com.example.gamesphere.enums.TopUpFulfillmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "top_up_fulfillments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpFulfillment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TopUpFulfillmentStatus status = TopUpFulfillmentStatus.PENDING;

    @Builder.Default
    @Column(nullable = false)
    private int attemptCount = 0;

    private String providerReference;

    @Column(length = 1000)
    private String failureReason;

    private LocalDateTime lastAttemptAt;

    private LocalDateTime completedAt;
}
