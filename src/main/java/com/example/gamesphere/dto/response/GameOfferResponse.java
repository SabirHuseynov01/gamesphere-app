package com.example.gamesphere.dto.response;

import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.Platform;
import com.example.gamesphere.enums.ProductStatus;
import com.example.gamesphere.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameOfferResponse {

    private Long productId;
    private String productName;
    private String editionName;
    private String productSlug;
    private Long sellerId;
    private String storeName;
    private String storeUrl;
    private String region;
    private String currency;
    private boolean officialStore;
    private LocalDateTime lastCheckedAt;
    private String sellerName;
    private Platform platform;
    private ProductType productType;
    private ProductStatus status;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal finalPrice;
    private Integer stockQuantity;
    private String keyProvider;
    private String imageUrl;
    private DeliveryType deliveryType;
    private String redemptionUrl;
    private Boolean requiresPlayerId;
    private String playerIdLabel;
    private String redemptionInstructions;
    private String inGameCurrencyName;
    private Integer inGameAmount;
    private Integer bonusAmount;
}
