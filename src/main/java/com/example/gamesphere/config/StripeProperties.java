package com.example.gamesphere.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.stripe")
public class StripeProperties {

    private boolean enabled = false;
    private String secretKey;
    private String webhookSecret;
    private String currency = "USD";
    private String successUrl = "http://localhost:3000/payment/success?session_id={CHECKOUT_SESSION_ID}";
    private String cancelUrl = "http://localhost:3000/payment/cancel";
}
