package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.TournamentCreateRequest;
import com.example.gamesphere.dto.request.TournamentJoinRequest;
import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.PageResponse;
import com.example.gamesphere.dto.response.TournamentParticipantResponse;
import com.example.gamesphere.dto.response.TournamentResponse;
import com.example.gamesphere.enums.TournamentStatus;
import com.example.gamesphere.service.TournamentParticipantService;
import com.example.gamesphere.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
@Tag(name = "Tournaments", description = "Tournament creation, filtering and participant operations")
@SecurityRequirement(name = "bearerAuth")
public class TournamentController {

    private final TournamentService tournamentService;
    private final TournamentParticipantService tournamentParticipantService;

    @PostMapping
    @Operation(summary = "Create tournament", description = "Creates a new tournament.")
    public ResponseEntity<ApiResponse<TournamentResponse>> createTournament(@Valid @RequestBody TournamentCreateRequest request) {
        TournamentResponse tournament = tournamentService.createTournament(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tournament created successfully", tournament));
    }

    @GetMapping
    @Operation(summary = "List tournaments", description = "Returns paginated tournament list.")
    public ResponseEntity<ApiResponse<PageResponse<TournamentResponse>>> getAllTournaments(Pageable pageable) {
        PageResponse<TournamentResponse> tournaments = tournamentService.getAllTournaments(pageable);
        return ResponseEntity.ok(ApiResponse.success("Tournaments retrieved", tournaments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tournament by id", description = "Returns tournament details by id.")
    public ResponseEntity<ApiResponse<TournamentResponse>> getTournament(@PathVariable Long id) {
        TournamentResponse tournament = tournamentService.getTournamentById(id);
        return ResponseEntity.ok(ApiResponse.success("Tournament retrieved", tournament));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filter tournaments by status", description = "Returns tournaments matching selected status.")
    public ResponseEntity<ApiResponse<List<TournamentResponse>>> getTournamentsByStatus(
            @PathVariable TournamentStatus status) {
        List<TournamentResponse> tournaments = tournamentService.getTournamentsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Tournaments retrieved by status", tournaments));
    }

    @GetMapping("/game/{game}")
    @Operation(summary = "Filter tournaments by game", description = "Returns tournaments for a selected game.")
    public ResponseEntity<ApiResponse<List<TournamentResponse>>> getTournamentsByGame(@PathVariable String game) {
        List<TournamentResponse> tournaments = tournamentService.getTournamentsByGame(game);
        return ResponseEntity.ok(ApiResponse.success("Tournaments retrieved by game", tournaments));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "List upcoming tournaments",
            description = "Returns tournaments starting after provided date or now.")
    public ResponseEntity<ApiResponse<List<TournamentResponse>>> getUpcomingTournaments(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after) {
        List<TournamentResponse> tournaments = tournamentService.getUpcomingTournamentsAfter(
                after != null ? after : LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success("Upcoming tournaments retrieved", tournaments));
    }

    @GetMapping("/creator/{creatorId}")
    @Operation(summary = "List creator tournaments", description = "Returns tournaments created by selected user.")
    public ResponseEntity<ApiResponse<List<TournamentResponse>>> getTournamentsByCreator(@PathVariable Long creatorId) {
        List<TournamentResponse> tournaments = tournamentService.getTournamentsByCreator(creatorId);
        return ResponseEntity.ok(ApiResponse.success("Creator tournaments retrieved", tournaments));
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "Join tournament", description = "Registers current user as a tournament participant.")
    public ResponseEntity<ApiResponse<TournamentParticipantResponse>> joinTournament(
            @PathVariable Long id,
            @Valid @RequestBody TournamentJoinRequest request) {
        TournamentParticipantResponse participant = tournamentParticipantService.joinTournament(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tournament joined successfully", participant));
    }

    @DeleteMapping("/{id}/leave")
    @Operation(summary = "Leave tournament", description = "Marks current user's tournament participation as left.")
    public ResponseEntity<ApiResponse<Void>> leaveTournament(@PathVariable Long id) {
        tournamentParticipantService.leaveTournament(id);
        return ResponseEntity.ok(ApiResponse.success("Tournament left successfully", null));
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "List tournament participants",
            description = "Returns participants registered for a tournament.")
    public ResponseEntity<ApiResponse<List<TournamentParticipantResponse>>> getTournamentParticipants(
            @PathVariable Long id) {
        List<TournamentParticipantResponse> participants = tournamentParticipantService.getParticipants(id);
        return ResponseEntity.ok(ApiResponse.success("Tournament participants retrieved", participants));
    }

    @GetMapping("/{id}/participants/me")
    @Operation(summary = "Get my tournament participation",
            description = "Returns current user's participation record for a tournament.")
    public ResponseEntity<ApiResponse<TournamentParticipantResponse>> getCurrentUserParticipation(
            @PathVariable Long id) {
        TournamentParticipantResponse participant = tournamentParticipantService.getCurrentUserParticipation(id);
        return ResponseEntity.ok(ApiResponse.success("Tournament participation retrieved", participant));
    }
}
