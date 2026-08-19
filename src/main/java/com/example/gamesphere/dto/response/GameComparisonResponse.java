package com.example.gamesphere.dto.response;

import com.example.gamesphere.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameComparisonResponse {

    private GameResponse game;
    private BigDecimal lowestPrice;
    private Integer offerCount;
    private Set<Platform> availablePlatforms;
    private List<GameOfferResponse> offers;
}
