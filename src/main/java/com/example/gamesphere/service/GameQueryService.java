package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.GameOfferCriteria;
import com.example.gamesphere.dto.request.GameSearchCriteria;
import com.example.gamesphere.entity.Game;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.repository.GameRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.specification.GameSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameQueryService {

    private final GameRepository gameRepository;
    private final ProductRepository productRepository;

    public Page<Game> searchGames(GameSearchCriteria criteria, Pageable pageable) {
        return gameRepository.findAll(GameSpecification.withSearchCriteria(criteria), pageable);
    }

    public List<Product> findOffers(Long gameId, GameOfferCriteria criteria) {
        return productRepository.findAll(GameSpecification.offersForGame(gameId, criteria));
    }
}
