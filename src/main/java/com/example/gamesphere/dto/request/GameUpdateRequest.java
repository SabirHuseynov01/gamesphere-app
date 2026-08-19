package com.example.gamesphere.dto.request;

import com.example.gamesphere.enums.GameAccessType;
import com.example.gamesphere.enums.GameGenre;
import com.example.gamesphere.enums.Platform;
import jakarta.validation.constraints.Size;
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
public class GameUpdateRequest {

    @Size(min = 1, max = 255)
    private String title;

    @Size(max = 2000)
    private String description;

    @Size(max = 1000)
    private String coverImageUrl;

    @Size(max = 255)
    private String developer;

    @Size(max = 255)
    private String publisher;

    private LocalDate releaseDate;

    private GameAccessType accessType;

    private Set<GameGenre> genres;

    private Set<Platform> supportedPlatforms;
}