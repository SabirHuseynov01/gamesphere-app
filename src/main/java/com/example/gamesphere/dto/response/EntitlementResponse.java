package com.example.gamesphere.dto.response;

import com.example.gamesphere.enums.EntitlementStatus;
import com.example.gamesphere.enums.Platform;
import com.example.gamesphere.enums.ProductType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntitlementResponse {

    private Long id;
    private Long productId;
    private Long gameId;
    private String productName;
    private ProductType productType;
    private Platform platform;
    private String imageUrl;
    private EntitlementStatus status;
    private LocalDateTime grantedAt;
}
