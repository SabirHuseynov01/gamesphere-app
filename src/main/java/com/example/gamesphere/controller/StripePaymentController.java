package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.CreateStripeCheckoutRequest;
import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.StripeCheckoutResponse;
import com.example.gamesphere.service.StripePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/stripe")
@RequiredArgsConstructor
@Tag(name = "Stripe Payments", description = "Stripe-hosted Checkout and verified webhook")
public class StripePaymentController {

    private final StripePaymentService stripePaymentService;

    @PostMapping("/checkout")
    @Operation(summary = "Create Stripe Checkout session")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<StripeCheckoutResponse>> createCheckout(
            @Valid @RequestBody CreateStripeCheckoutRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Stripe Checkout created", stripePaymentService.createCheckout(request)));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Receive Stripe webhook", hidden = true)
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        stripePaymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
