package com.example.gamesphere.entity;

import com.example.gamesphere.enums.Platform;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.ProductStatus;
import com.example.gamesphere.enums.ProductType;
import com.example.gamesphere.enums.CatalogSection;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(nullable = false)
    private String name;

    private String editionName;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal discountPrice;

    @Column(nullable = false)
    private int stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    private String genre;

    private String imageUrl;
    private String keyProvider; //Steam, GOG, Epic Games,

    private String storeName;

    @Column(length = 1000)
    private String storeUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeliveryType deliveryType = DeliveryType.STORE_REDIRECT;

    @Column(length = 1000)
    private String redemptionUrl;

    @Builder.Default
    private boolean requiresPlayerId = false;

    private String playerIdLabel;

    @Column(length = 1000)
    private String redemptionInstructions;

    private String inGameCurrencyName;

    private Integer inGameAmount;

    private Integer bonusAmount;

    @Builder.Default
    private String region = "GLOBAL";

    @Builder.Default
    private String currency = "USD";

    @Builder.Default
    private boolean officialStore = false;

    @Builder.Default
    private LocalDateTime lastCheckedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductType productType = ProductType.GAME;

    @Enumerated(EnumType.STRING)
    @Column(name = "catalog_section")
    private CatalogSection catalogSection;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToMany
    @JoinTable(name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @Builder.Default
    private boolean isActive = true;
}
