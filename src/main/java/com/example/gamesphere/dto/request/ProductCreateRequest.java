package com.example.gamesphere.dto.request;

import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.Platform;
import com.example.gamesphere.enums.ProductType;
import com.example.gamesphere.enums.CatalogSection;
import com.example.gamesphere.validation.ValidDiscountPrice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidDiscountPrice
public class ProductCreateRequest {

    @NotBlank
    private String name;

    private String editionName;

    private Long gameId;
    private String gameTitle;

    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    private BigDecimal discountPrice;

    @NotNull
    @PositiveOrZero
    private Integer stockQuantity;

    @NotNull
    private Platform platform;

    private ProductType productType;

    private CatalogSection catalogSection;

    private String genre;
    private String keyProvider;

    @NotBlank
    private String storeName;

    @NotBlank
    private String storeUrl;

    private String region;
    private String currency;
    private Boolean officialStore;
    private DeliveryType deliveryType;
    private String redemptionUrl;
    private Boolean requiresPlayerId;
    private String playerIdLabel;
    private String redemptionInstructions;
    private String inGameCurrencyName;
    private Integer inGameAmount;
    private Integer bonusAmount;
    private Set<Long> categoryIds;
}
