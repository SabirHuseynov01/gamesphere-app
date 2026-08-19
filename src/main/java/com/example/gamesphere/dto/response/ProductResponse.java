package com.example.gamesphere.dto.response;

import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.Platform;
import com.example.gamesphere.enums.ProductStatus;
import com.example.gamesphere.enums.ProductType;
import com.example.gamesphere.enums.CatalogSection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String editionName;
    private String slug;
    private Long gameId;
    private String gameTitle;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal finalPrice;
    private BigDecimal discountPercentage;
    private Integer stockQuantity;
    private Platform platform;
    private ProductType productType;
    private CatalogSection catalogSection;
    private ProductStatus status;
    private String genre;
    private String imageUrl;
    private String keyProvider;
    private String storeName;
    private String storeUrl;
    private String region;
    private String currency;
    private boolean officialStore;
    private DeliveryType deliveryType;
    private String redemptionUrl;
    private Boolean requiresPlayerId;
    private String playerIdLabel;
    private String redemptionInstructions;
    private String inGameCurrencyName;
    private Integer inGameAmount;
    private Integer bonusAmount;
    private LocalDateTime lastCheckedAt;
    private boolean isActive;
    private LocalDateTime createdAt;
    private Set<String> categories;

}
