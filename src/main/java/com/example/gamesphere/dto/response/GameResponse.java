package com.example.gamesphere.dto.response;

import com.example.gamesphere.enums.GameAccessType;
import com.example.gamesphere.enums.GameGenre;
import com.example.gamesphere.enums.GameCatalogType;
import com.example.gamesphere.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameResponse {

    private Long id;
    private String title;
    private String slug;
    private String description;
    private String coverImageUrl;
    private String developer;
    private String publisher;
    private LocalDate releaseDate;
    private GameAccessType accessType;
    private GameCatalogType catalogType;
    private Set<GameGenre> genres;
    private Set<Platform> supportedPlatforms;
}
