package com.example.gamesphere.dto.request;

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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductFilterRequest {

    private String search;
    private String editionName;
    private String genre;
    private Platform platform;
    private ProductType productType;
    private CatalogSection catalogSection;
    private DeliveryType deliveryType;
    private String inGameCurrencyName;
    private Integer minInGameAmount;
    private Integer maxInGameAmount;
    private ProductStatus status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStock;
}
