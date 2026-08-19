package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.GameOfferCriteria;
import com.example.gamesphere.dto.request.GameSearchCriteria;
import com.example.gamesphere.entity.Game;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.repository.GameRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.service.GameQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameQueryServiceTest {

    @Mock GameRepository gameRepository;
    @Mock ProductRepository productRepository;
    @InjectMocks
    GameQueryService gameQueryService;

    @Test
    void searchGamesExecutesSpecificationAtRepositoryLevel() {
        GameSearchCriteria criteria = new GameSearchCriteria();
        criteria.setQ("battlefield");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Game> expected = new PageImpl<>(List.of(new Game()));
        when(gameRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expected);

        assertThat(gameQueryService.searchGames(criteria, pageable)).isSameAs(expected);
        verify(gameRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findOffersUsesDatabaseSpecification() {
        GameOfferCriteria criteria = new GameOfferCriteria();
        List<Product> expected = List.of(new Product());
        when(productRepository.findAll(any(Specification.class))).thenReturn(expected);

        assertThat(gameQueryService.findOffers(5L, criteria)).isSameAs(expected);
        verify(productRepository).findAll(any(Specification.class));
    }
}
