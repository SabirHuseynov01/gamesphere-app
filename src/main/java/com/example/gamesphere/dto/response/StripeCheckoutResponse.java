package com.example.gamesphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class StripeCheckoutResponse {
    private String sessionId;
    private String checkoutUrl;
    private Instant expiresAt;
}
