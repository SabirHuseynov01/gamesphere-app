package com.example.gamesphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageResponse {

    private Long id;
    private Long productId;
    private String imageUrl;
    private int sortOther;
    private boolean primary;
}
