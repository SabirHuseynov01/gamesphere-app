package com.example.gamesphere.dto.request;

import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.GameOfferSort;
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
public class GameOfferCriteria {

    private Platform platform;
    private ProductType productType;
    private DeliveryType deliveryType;
    private String inGameCurrencyName;
    private Integer minInGameAmount;
    private Integer maxInGameAmount;
    private String storeName;
    private String region;
    private String currency;
    private Boolean officialStore;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStock;
    private GameOfferSort sort = GameOfferSort.PRICE_ASC;
}
