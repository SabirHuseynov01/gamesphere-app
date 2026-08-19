package com.example.gamesphere.dto.response;

import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.EntitlementStatus;
import com.example.gamesphere.enums.Platform;
import com.example.gamesphere.enums.ProductType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LibraryItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String imageUrl;
    private Platform platform;
    private ProductType productType;
    private DeliveryType deliveryType;
    private EntitlementStatus status;
    private LocalDateTime grantedAt;
    private String digitalCode;
    private String redemptionUrl;
    private String redemptionInstructions;
}
