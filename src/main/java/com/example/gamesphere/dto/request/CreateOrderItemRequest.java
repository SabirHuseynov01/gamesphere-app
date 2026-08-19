package com.example.gamesphere.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderItemRequest {

    @NotNull
    private Long productId;


    @Positive
    private Integer quantity = 1;

    private boolean gift;

    @Email
    private String recipientEmail;

    @Size(max = 500)
    private String giftMessage;

    @Size(max = 255)
    private String playerAccountId;

    @AssertTrue(message = "Recipient email is required for gift orders")
    public boolean isGiftRecipientValid() {
        return !gift || (recipientEmail != null && !recipientEmail.isBlank());
    }

    public int resolvedQuantity() {
        return quantity == null ? 1 : quantity;
    }
}
