package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.GameSearchCriteria;
import com.example.gamesphere.dto.request.ProductFilterRequest;
import com.example.gamesphere.dto.response.GlobalSearchResponse;
import com.example.gamesphere.dto.response.SearchGameResponse;
import com.example.gamesphere.dto.response.SearchProductResponse;
import com.example.gamesphere.entity.Game;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.repository.GameRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.specification.GameSpecification;
import com.example.gamesphere.specification.ProductSpecification;
import com.example.gamesphere.util.DiscountCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final GameRepository gameRepository;
    private final ProductRepository productRepository;
    private final DiscountCalculator discountCalculator;

    @Transactional(readOnly = true)
    public GlobalSearchResponse search (String query, int limit ) {
        String safeQuery = query == null ? "" : query.trim();
        int safeLimit = Math.max(1, Math.min(limit, 20));
        if (safeQuery.isBlank()) {
            return new GlobalSearchResponse(safeQuery, List.of(), List.of());
        }
        Pageable pageable = PageRequest.of(0, safeLimit);

        GameSearchCriteria gameCriteria = new GameSearchCriteria();
        gameCriteria.setQ(safeQuery);

        ProductFilterRequest productFilter = new ProductFilterRequest();
        productFilter.setSearch(safeQuery);

        List<SearchGameResponse> games = gameRepository.findAll(
                GameSpecification.withSearchCriteria(gameCriteria),
                pageable)
                .map(this::toGameResponse)
                .toList();

        List<SearchProductResponse> products = productRepository.findAll(
                ProductSpecification.withFilter(productFilter),
                pageable)
                .map(this::toProductResponse)
                .toList();

        return new GlobalSearchResponse(safeQuery, games, products);

    }

    private SearchGameResponse toGameResponse(Game game) {
        return new SearchGameResponse(
               game.getId(),
                game.getTitle(),
                game.getSlug(),
                game.getCoverImageUrl(),
                game.getDeveloper(),
                game.getPublisher(),
                game.getReleaseDate()
        );
    }

    private SearchProductResponse toProductResponse(Product product) {
        return new SearchProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getGame() != null ? product.getGame().getId() : null,
                product.getGame() != null ? product.getGame().getTitle() : null,
                product.getImageUrl(),
                product.getPlatform(),
                product.getProductType(),
                product.getDeliveryType(),
                product.getPrice(),
                product.getDiscountPrice(),
                discountCalculator.finalPrice(product.getPrice(), product.getDiscountPrice()),
                product.getCurrency(),
                product.getStoreName(),
                product.getStoreUrl(),
                product.getInGameCurrencyName(),
                product.getInGameAmount(),
                product.getBonusAmount());
    }
}
