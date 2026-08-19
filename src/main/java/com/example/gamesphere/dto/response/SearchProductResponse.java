package com.example.gamesphere.dto.response;

import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.Platform;
import com.example.gamesphere.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchProductResponse {

    private Long id;
    private String name;
    private String slug;
    private Long gameId;
    private String gameTitle;
    private String imageUrl;
    private Platform platform;
    private ProductType productType;
    private DeliveryType deliveryType;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal finalPrice;
    private String currency;
    private String storeName;
    private String storeUrl;
    private String inGameCurrencyName;
    private Integer inGameAmount;
    private Integer bonusAmount;
}
