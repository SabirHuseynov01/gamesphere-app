package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.GameCreateRequest;
import com.example.gamesphere.dto.request.GameOfferCriteria;
import com.example.gamesphere.dto.request.GameSearchCriteria;
import com.example.gamesphere.dto.request.GameUpdateRequest;
import com.example.gamesphere.dto.response.GameComparisonResponse;
import com.example.gamesphere.dto.response.GameOfferResponse;
import com.example.gamesphere.dto.response.GameResponse;
import com.example.gamesphere.dto.response.PageResponse;
import com.example.gamesphere.entity.Game;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.enums.GameAccessType;
import com.example.gamesphere.enums.GameCatalogType;
import com.example.gamesphere.enums.Platform;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.repository.GameRepository;
import com.example.gamesphere.util.DiscountCalculator;
import com.example.gamesphere.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameQueryService gameQueryService;
    private final SlugGenerator slugGenerator;
    private final DiscountCalculator discountCalculator;
    private final FileStorageService fileStorageService;

    @Transactional
    public GameResponse createGame(GameCreateRequest request) {
        Game game = new Game();
        game.setTitle(request.getTitle());
        game.setSlug(generateUniqueSlug(request.getTitle()));
        game.setDescription(request.getDescription());
        game.setCoverImageUrl(request.getCoverImageUrl());
        game.setDeveloper(request.getDeveloper());
        game.setPublisher(request.getPublisher());
        game.setReleaseDate(request.getReleaseDate());
        game.setAccessType(request.getAccessType() == null ? GameAccessType.PAID : request.getAccessType());
        game.setCatalogType(request.getCatalogType() == null ? GameCatalogType.GAME : request.getCatalogType());
        replaceGenres(game, request.getGenres());
        replaceSupportedPlatforms(game, request.getSupportedPlatforms());

        return toGameResponse(gameRepository.save(game));
    }

    @Transactional
    public GameResponse updateGame(Long gameId, GameUpdateRequest request) {
        Game game = getGame(gameId);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            game.setTitle(request.getTitle().trim());
            game.setSlug(generateUniqueSlug(game.getTitle(), game.getId()));
        }
        if (request.getDescription() != null) {
            game.setDescription(request.getDescription());
        }
        if (request.getCoverImageUrl() != null) {
            game.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getDeveloper() != null) {
            game.setDeveloper(request.getDeveloper());
        }
        if (request.getPublisher() != null) {
            game.setPublisher(request.getPublisher());
        }
        if (request.getReleaseDate() != null) {
            game.setReleaseDate(request.getReleaseDate());
        }
        if (request.getAccessType() != null) {
            game.setAccessType(request.getAccessType());
        }
        if (request.getCatalogType() != null) {
            game.setCatalogType(request.getCatalogType());
        }
        if (request.getGenres() != null) {
            replaceGenres(game, request.getGenres());
        }
        if (request.getSupportedPlatforms() != null) {
            replaceSupportedPlatforms(game, request.getSupportedPlatforms());
        }

        return toGameResponse(gameRepository.save(game));
    }

    @Transactional
    public void deleteGame(Long gameId) {
        Game game = getGame(gameId);
        game.setDeleted(true);
        gameRepository.save(game);
    }

    @Transactional
    public GameResponse uploadCover(Long gameId, MultipartFile file) {
        Game game = getGame(gameId);
        game.setCoverImageUrl(fileStorageService.storeGameCover(gameId, file));
        return toGameResponse(gameRepository.save(game));
    }

    @Transactional(readOnly = true)
    public PageResponse<GameResponse> searchGames(GameSearchCriteria criteria, Pageable pageable) {
        Page<Game> games = gameQueryService.searchGames(criteria, pageable);
        return PageResponse.of(games.map(this::toGameResponse));
    }

    @Transactional(readOnly = true)
    public GameResponse getGameBySlug(String slug) {
        Game game = gameRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + slug));
        return toGameResponse(game);
    }

    @Transactional(readOnly = true)
    public GameComparisonResponse compareGameOffers(
            String slug,
            GameOfferCriteria criteria) {

        Game game = gameRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + slug));

        List<Product> products = gameQueryService.findOffers(game.getId(), criteria);
        List<GameOfferResponse> offers = products.stream()
                .map(this::toOfferResponse)
                .toList();

        List<GameOfferResponse> baseGameOffers = offers.stream()
                .filter(offer -> offer.getProductType() == com.example.gamesphere.enums.ProductType.GAME)
                .toList();

        BigDecimal lowestPrice = baseGameOffers.stream()
                .map(GameOfferResponse::getFinalPrice)
                .filter(java.util.Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);

        Set<Platform> availablePlatforms = baseGameOffers.stream()
                .map(GameOfferResponse::getPlatform)
                .collect(Collectors.toSet());

        return new GameComparisonResponse(
                toGameResponse(game),
                lowestPrice,
                baseGameOffers.size(),
                availablePlatforms,
                offers);
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = slugGenerator.generate(title);
        String slug = baseSlug;
        int counter = 2;

        while (gameRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }

    private String generateUniqueSlug(String title, Long currentGameId) {
        String baseSlug = slugGenerator.generate(title);
        String slug = baseSlug;
        int counter = 2;

        while (gameRepository.findBySlug(slug)
                .filter(existing -> !existing.getId().equals(currentGameId))
                .isPresent()) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }

    private Game getGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
    }

    private GameResponse toGameResponse(Game game) {
        return new GameResponse(
                game.getId(),
                game.getTitle(),
                game.getSlug(),
                game.getDescription(),
                game.getCoverImageUrl(),
                game.getDeveloper(),
                game.getPublisher(),
                game.getReleaseDate(),
                game.getAccessType(),
                game.getCatalogType(),
                Set.copyOf(game.getGenres()),
                Set.copyOf(game.getSupportedPlatforms()));
    }

    private void replaceGenres(Game game, Set<com.example.gamesphere.enums.GameGenre> genres) {
        game.setGenres(genres == null ? new HashSet<>() : new HashSet<>(genres));
    }

    private void replaceSupportedPlatforms(Game game, Set<Platform> supportedPlatforms) {
        game.setSupportedPlatforms(supportedPlatforms == null
                ? new HashSet<>()
                : new HashSet<>(supportedPlatforms));
    }

    private GameOfferResponse toOfferResponse(Product product) {
        GameOfferResponse response = new GameOfferResponse();
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setProductSlug(product.getSlug());
        response.setSellerId(product.getSeller() != null ? product.getSeller().getId() : null);
        response.setStoreName(product.getStoreName());
        response.setStoreUrl(product.getStoreUrl());
        response.setRegion(product.getRegion());
        response.setCurrency(product.getCurrency());
        response.setOfficialStore(product.isOfficialStore());
        response.setLastCheckedAt(product.getLastCheckedAt());
        response.setPlatform(product.getPlatform());
        response.setProductType(product.getProductType());
        response.setStatus(product.getStatus());
        response.setPrice(product.getPrice());
        response.setDiscountPrice(product.getDiscountPrice());
        response.setFinalPrice(finalPrice(product));
        response.setStockQuantity(product.getStockQuantity());
        response.setKeyProvider(product.getKeyProvider());
        response.setImageUrl(product.getImageUrl());
        response.setDeliveryType(product.getDeliveryType());
        response.setRedemptionUrl(product.getRedemptionUrl());
        response.setRequiresPlayerId(product.isRequiresPlayerId());
        response.setPlayerIdLabel(product.getPlayerIdLabel());
        response.setRedemptionInstructions(product.getRedemptionInstructions());
        response.setInGameCurrencyName(product.getInGameCurrencyName());
        response.setInGameAmount(product.getInGameAmount());
        response.setBonusAmount(product.getBonusAmount());
        return response;
    }

    private BigDecimal finalPrice(Product product) {
        return discountCalculator.finalPrice(product.getPrice(), product.getDiscountPrice());
    }
}