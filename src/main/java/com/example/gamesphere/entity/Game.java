package com.example.gamesphere.entity;

import com.example.gamesphere.enums.GameAccessType;
import com.example.gamesphere.enums.GameGenre;
import com.example.gamesphere.enums.GameCatalogType;
import com.example.gamesphere.enums.Platform;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "games")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Game extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 2000)
    private String description;

    private String coverImageUrl;
    private String developer;
    private String publisher;
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false)
    @Builder.Default
    private GameAccessType accessType = GameAccessType.PAID;

    @Enumerated(EnumType.STRING)
    @Column(name = "catalog_type", nullable = false)
    @Builder.Default
    private GameCatalogType catalogType = GameCatalogType.GAME;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "game_genres", joinColumns = @JoinColumn(name = "game_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    @Builder.Default
    private Set<GameGenre> genres = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "game_supported_platforms", joinColumns = @JoinColumn(name = "game_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    @Builder.Default
    private Set<Platform> supportedPlatforms = new HashSet<>();
}

