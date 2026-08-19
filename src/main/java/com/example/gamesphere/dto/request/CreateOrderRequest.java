package com.example.gamesphere.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    private List<Long> productIds;

    @Valid
    private List<CreateOrderItemRequest> items;

    @AssertTrue(message = "Order must contain at least one product")
    public boolean hasProducts() {
        return (productIds != null && !productIds.isEmpty())
                || (items != null && !items.isEmpty());
    }
}
