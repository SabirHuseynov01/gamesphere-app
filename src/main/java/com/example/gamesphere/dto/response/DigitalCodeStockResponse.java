package com.example.gamesphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DigitalCodeStockResponse {

    private Long productId;
    private int addedCount;
    private long availableCount;
}
