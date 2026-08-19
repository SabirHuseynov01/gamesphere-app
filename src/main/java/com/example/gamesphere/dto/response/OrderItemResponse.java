package com.example.gamesphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private boolean gift;
    private String giftRecipientEmail;
    private String giftMessage;
    private String playerAccountId;
}
