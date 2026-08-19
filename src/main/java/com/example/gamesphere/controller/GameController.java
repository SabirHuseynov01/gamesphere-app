package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.GameCreateRequest;
import com.example.gamesphere.dto.request.GameOfferCriteria;
import com.example.gamesphere.dto.request.GameSearchCriteria;
import com.example.gamesphere.dto.request.GameUpdateRequest;
import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.GameComparisonResponse;
import com.example.gamesphere.dto.response.GameResponse;
import com.example.gamesphere.dto.response.PageResponse;
import com.example.gamesphere.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Tag(name = "Games", description = "Canonical game search and platform offer comparison")
public class GameController {

    private final GameService gameService;

    @PostMapping
    @Operation(summary = "Create game", description = "Creates a canonical game entry used to group platform offers.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<GameResponse>> createGame(@Valid @RequestBody GameCreateRequest request) {
        GameResponse game = gameService.createGame(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Game created", game));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update game", description = "Updates canonical game metadata, including an external cover URL.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<GameResponse>> updateGame(
            @PathVariable Long id,
            @Valid @RequestBody GameUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Game updated",
                gameService.updateGame(id, request)));
    }

    @PostMapping("/{id}/cover")
    @Operation(summary = "Upload game cover", description = "Stores a cover image locally and updates the game's cover URL.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<GameResponse>> uploadGameCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Game cover uploaded", gameService.uploadCover(id, file)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search games", description = "Searches canonical games by title, for example Battlefield 6.")
    public ResponseEntity<ApiResponse<PageResponse<GameResponse>>> searchGames(
            @ModelAttribute GameSearchCriteria criteria,
            Pageable pageable) {
        PageResponse<GameResponse> games = gameService.searchGames(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("Games searched", games));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get game by slug", description = "Returns canonical game details by slug.")
    public ResponseEntity<ApiResponse<GameResponse>> getGameBySlug(@PathVariable String slug) {
        GameResponse game = gameService.getGameBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Game retrieved", game));
    }

    @GetMapping("/slug/{slug}/offers")
    @Operation(summary = "Compare game offers", description = "Lists all seller/platform offers for a game with optional platform/type filtering and sorting.")
    public ResponseEntity<ApiResponse<GameComparisonResponse>> compareGameOffers(
            @PathVariable String slug,
            @ModelAttribute GameOfferCriteria criteria) {
        GameComparisonResponse comparison = gameService.compareGameOffers(slug, criteria);
        return ResponseEntity.ok(ApiResponse.success("Game offers compared", comparison));
    }
}
