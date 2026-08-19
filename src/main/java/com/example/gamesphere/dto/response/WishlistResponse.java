package com.example.gamesphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {

    private Long wishlistId;
    private List<ProductResponse> products;
}
