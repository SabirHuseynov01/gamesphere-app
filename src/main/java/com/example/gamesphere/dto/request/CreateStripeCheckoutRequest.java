package com.example.gamesphere.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStripeCheckoutRequest {

    @NotNull
    private Long orderId;
}
