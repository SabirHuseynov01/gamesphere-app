package com.example.gamesphere.dto.response;

import com.example.gamesphere.enums.TopUpFulfillmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopUpFulfillmentResponse {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long orderItemId;
    private Long productId;
    private String productName;
    private String playerAccountId;
    private String inGameCurrencyName;
    private Integer inGameAmount;
    private Integer bonusAmount;
    private TopUpFulfillmentStatus status;
    private int attemptCount;
    private String providerReference;
    private String failureReason;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
