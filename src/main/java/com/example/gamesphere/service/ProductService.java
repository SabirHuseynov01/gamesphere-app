package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.ProductCreateRequest;
import com.example.gamesphere.dto.request.ProductUpdateRequest;
import com.example.gamesphere.dto.response.ProductResponse;
import com.example.gamesphere.entity.Game;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.Category;
import com.example.gamesphere.entity.SellerProfile;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.ProductStatus;
import com.example.gamesphere.enums.ProductType;
import com.example.gamesphere.enums.CatalogSection;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.ProductMapper;
import com.example.gamesphere.repository.GameRepository;
import com.example.gamesphere.repository.CategoryRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.SellerProfileRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.util.DiscountCalculator;
import com.example.gamesphere.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final ProductMapper productMapper;
    private final SlugGenerator slugGenerator;
    private final DiscountCalculator discountCalculator;

    @Transactional
    @CacheEvict(value = {"activeProducts", "productRecommendations"}, allEntries = true)
    public ProductResponse createProduct(ProductCreateRequest request) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User seller = resolveProductOwner(currentEmail, request);

        Product product = productMapper.toEntity(request);
        product.setSeller(seller);
        product.setGame(resolveGame(request.getGameId(), request.getGameTitle(), request.getName()));
        product.setCategories(resolveCategories(request.getCategoryIds()));
        product.setSlug(generateUniqueSlug(request.getName(), request.getPlatform().name()));
        product.setProductType(request.getProductType() != null ? request.getProductType() : ProductType.GAME);
        product.setCatalogSection(request.getCatalogSection() != null
                ? request.getCatalogSection()
                : defaultCatalogSection(product.getProductType()));
        product.setStatus(ProductStatus.ACTIVE);
        product.setActive(true);
        applyAggregatorDefaults(product);
        applyDeliveryDefaults(product);
        validateDigitalOfferMetadata(product);
        rejectDuplicateEditionOffer(product, null);
        discountCalculator.validateDiscount(product.getPrice(), product.getDiscountPrice());

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    @CacheEvict(value = {"activeProducts", "productRecommendations"}, allEntries = true)
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean admin = isAdmin();

        if (!admin) {
            User seller = getApprovedSeller(currentUser);
            if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
                throw new BusinessException("You can only update your own products.");
            }
        }

        productMapper.updateEntity(product, request);
        if (request.getCatalogSection() == null && product.getCatalogSection() == null) {
            product.setCatalogSection(defaultCatalogSection(product.getProductType()));
        }
        if (request.getGameId() != null || request.getGameTitle() != null) {
            product.setGame(resolveGame(request.getGameId(), request.getGameTitle(), product.getName()));
        }
        if (request.getCategoryIds() != null) {
            product.setCategories(resolveCategories(request.getCategoryIds()));
        }
        if (request.getName() != null || request.getPlatform() != null) {
            product.setSlug(generateUniqueSlug(product.getName(), product.getPlatform().name(), product.getId()));
        }
        if (product.getStockQuantity() <= 0 && product.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }
        if (product.getStatus() == ProductStatus.DELETED) {
            product.setDeleted(true);
            product.setActive(false);
        }
        applyAggregatorDefaults(product);
        applyDeliveryDefaults(product);

        if (admin && !isAdminManagedPaidRedirectGame(product)) {
            throw new BusinessException("Admins can update only GAME marketplace store redirect products.");
        }

        validateDigitalOfferMetadata(product);
        rejectDuplicateEditionOffer(product, product.getId());
        product.setLastCheckedAt(LocalDateTime.now());
        discountCalculator.validateDiscount(product.getPrice(), product.getDiscountPrice());
        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    @Transactional
    @CacheEvict(value = {"activeProducts", "productRecommendations"}, allEntries = true)
    public ProductResponse deactivateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setStatus(ProductStatus.INACTIVE);
        product.setActive(false);
        return productMapper.toResponse(productRepository.save(product));
    }

    private String generateUniqueSlug(String name, String platform) {
        return generateUniqueSlug(name, platform, null);
    }

    private User resolveProductOwner(String email, ProductCreateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (isAdmin() && isAdminManagedPaidRedirectGame(request)) {
            return user;
        }

        return getApprovedSeller(user);
    }

    private User getApprovedSeller(User seller) {
        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(seller.getId())
                .orElseThrow(() -> new BusinessException(
                        "Seller profile not found. Please create a seller profile first."));

        if (!seller.isSeller() || !sellerProfile.isApproved()) {
            throw new BusinessException("Your seller profile has not been approved yet.");
        }

        return seller;
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private boolean isAdminManagedPaidRedirectGame(ProductCreateRequest request) {
        ProductType productType = request.getProductType() != null ? request.getProductType() : ProductType.GAME;
        CatalogSection catalogSection = request.getCatalogSection() != null
                ? request.getCatalogSection()
                : defaultCatalogSection(productType);
        DeliveryType deliveryType = request.getDeliveryType() != null
                ? request.getDeliveryType()
                : DeliveryType.STORE_REDIRECT;

        return productType == ProductType.GAME
                && catalogSection == CatalogSection.MARKETPLACE
                && deliveryType == DeliveryType.STORE_REDIRECT;
    }

    private boolean isAdminManagedPaidRedirectGame(Product product) {
        return product.getProductType() == ProductType.GAME
                && product.getCatalogSection() == CatalogSection.MARKETPLACE
                && product.getDeliveryType() == DeliveryType.STORE_REDIRECT;
    }

    private void applyAggregatorDefaults(Product product) {
        if (product.getRegion() == null || product.getRegion().isBlank()) {
            product.setRegion("GLOBAL");
        }
        if (product.getCurrency() == null || product.getCurrency().isBlank()) {
            product.setCurrency("USD");
        }
        if (product.getLastCheckedAt() == null) {
            product.setLastCheckedAt(LocalDateTime.now());
        }
    }

    private CatalogSection defaultCatalogSection(ProductType productType) {
        return isTopUpType(productType) ? CatalogSection.TOP_UPS : CatalogSection.MARKETPLACE;
    }

    private boolean isTopUpType(ProductType productType) {
        return productType == ProductType.IN_GAME_CURRENCY
                || productType == ProductType.IN_GAME_ITEM
                || productType == ProductType.CURRENCY
                || productType == ProductType.ITEM
                || productType == ProductType.BATTLE_PASS
                || productType == ProductType.GIFT_CARD
                || productType == ProductType.SUBSCRIPTION;
    }

    private void applyDeliveryDefaults(Product product) {
        if (product.getDeliveryType() == null) {
            product.setDeliveryType(DeliveryType.STORE_REDIRECT);
        }

        if (product.getDeliveryType() == DeliveryType.PLAYER_ID_TOP_UP) {
            product.setRequiresPlayerId(true);

            if (product.getPlayerIdLabel() == null || product.getPlayerIdLabel().isBlank()) {
                product.setPlayerIdLabel("Player ID");
            }

        }
    }

    /**
     * The database carries a partial unique index over
     * (game, platform, store, edition) — changeset 016. Without this check a
     * second offer for the same edition reaches the insert and comes back as a
     * constraint violation, which the handler can only answer with a 500.
     * Catching it here turns that into a 400 that says what is wrong.
     *
     * @param selfId id of the product being updated, or null when creating.
     */
    private void rejectDuplicateEditionOffer(Product product, Long selfId) {
        if (product.getGame() == null
                || product.getPlatform() == null
                || product.getEditionName() == null || product.getEditionName().isBlank()
                || product.getStoreName() == null || product.getStoreName().isBlank()) {
            return;
        }

        productRepository
                .findByGameIdAndPlatformAndStoreNameIgnoreCaseAndEditionNameIgnoreCaseAndIsDeletedFalse(
                        product.getGame().getId(),
                        product.getPlatform(),
                        product.getStoreName(),
                        product.getEditionName())
                .filter(existing -> selfId == null || !selfId.equals(existing.getId()))
                .ifPresent(existing -> {
                    throw new BusinessException(
                            "A " + product.getEditionName() + " edition offer already exists for "
                                    + product.getStoreName() + " on " + product.getPlatform() + ".");
                });
    }

    private void validateDigitalOfferMetadata(Product product) {
        if (product.getDeliveryType() == DeliveryType.STORE_REDIRECT
                && (product.getStoreName() == null || product.getStoreName().isBlank()
                || product.getStoreUrl() == null || product.getStoreUrl().isBlank())) {
            throw new BusinessException("Store name and store URL are required for redirect products.");
        }

        if (product.getDeliveryType() == DeliveryType.EXTERNAL_MARKET
                && (product.getStoreUrl() == null || product.getStoreUrl().isBlank())) {
            throw new BusinessException("Store URL is required for external marketplace products.");
        }

        if (product.getInGameAmount() != null && product.getInGameAmount() <= 0) {
            throw new BusinessException("In-game amount must be greater than zero.");
        }

        if (product.getBonusAmount() != null && product.getBonusAmount() < 0) {
            throw new BusinessException("Bonus amount cannot be negative.");
        }
    }

    private Game resolveGame(Long gameId, String gameTitle, String fallbackTitle) {
        if (gameId != null) {
            return gameRepository.findById(gameId)
                    .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
        }

        String title = gameTitle != null && !gameTitle.isBlank() ? gameTitle : fallbackTitle;
        String baseSlug = slugGenerator.generate(title);

        return gameRepository.findBySlug(baseSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Game not found: " + title + ". An admin must create the game first."));
    }

    private Set<Category> resolveCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new HashSet<>();
        }
        if (categoryIds.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException("Category ids cannot contain null.");
        }

        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new ResourceNotFoundException("One or more categories were not found.");
        }
        return new HashSet<>(categories);
    }

    private String generateUniqueSlug(String name, String platform, Long currentProductId) {
        String baseSlug = slugGenerator.generate(name + "-" + platform);
        String slug = baseSlug;
        int counter = 2;

        if (currentProductId == null) {
            while (productRepository.existsBySlug(slug)) {
                slug = baseSlug + "-" + counter++;
            }
            return slug;
        }

        while (productRepository.findBySlug(slug)
                .filter(existing -> !existing.getId().equals(currentProductId))
                .isPresent()) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }
}