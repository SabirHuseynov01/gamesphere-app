package com.example.gamesphere.dto.response;

import com.example.gamesphere.enums.GiftStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GiftResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Long senderId;
    private String senderUsername;
    private String recipientEmail;
    private String message;
    private String claimToken;
    private GiftStatus status;
    private LocalDateTime claimedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
