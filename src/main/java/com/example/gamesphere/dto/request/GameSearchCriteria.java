package com.example.gamesphere.dto.request;

import com.example.gamesphere.enums.GameAccessType;
import com.example.gamesphere.enums.GameBrowseSort;
import com.example.gamesphere.enums.GameGenre;
import com.example.gamesphere.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameSearchCriteria {

    private String q;
    private String title;
    private String developer;
    private String publisher;
    private LocalDate releaseDateFrom;
    private LocalDate releaseDateTo;
    private GameAccessType accessType;
    private Set<GameGenre> genres;
    private Platform platform;
    private BigDecimal minOfferPrice;
    private BigDecimal maxOfferPrice;
    private GameBrowseSort sortBy = GameBrowseSort.ALPHABETICAL;
}