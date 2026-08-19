package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.TournamentCreateRequest;
import com.example.gamesphere.dto.response.PageResponse;
import com.example.gamesphere.dto.response.TournamentResponse;
import com.example.gamesphere.entity.Tournament;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.TournamentStatus;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.TournamentMapper;
import com.example.gamesphere.repository.TournamentRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentMapper tournamentMapper;
    private final UserRepository userRepository;

    @Transactional
    public TournamentResponse createTournament(TournamentCreateRequest request) {
        Tournament tournament = tournamentMapper.toEntity(request);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament creator not found"));
        tournament.setCreator(creator);
        Tournament savedTournament = tournamentRepository.save(tournament);
        return tournamentMapper.toResponse(savedTournament);
    }

    public PageResponse<TournamentResponse> getAllTournaments(Pageable pageable) {
        Page<Tournament> tournaments = tournamentRepository.findAll(pageable);
        return PageResponse.of(tournaments.map(tournamentMapper::toResponse));
    }

    public TournamentResponse getTournamentById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));
        return tournamentMapper.toResponse(tournament);
    }

    public List<TournamentResponse> getTournamentsByStatus(TournamentStatus status) {
        return tournamentRepository.findByStatus(status).stream()
                .map(tournamentMapper::toResponse)
                .toList();
    }

    public List<TournamentResponse> getTournamentsByGame(String game) {
        return tournamentRepository.findByGame(game).stream()
                .map(tournamentMapper::toResponse)
                .toList();
    }

    public List<TournamentResponse> getUpcomingTournamentsAfter(LocalDateTime date) {
        return tournamentRepository.findByStartDateAfter(date).stream()
                .map(tournamentMapper::toResponse)
                .toList();
    }

    public List<TournamentResponse> getTournamentsByCreator(Long creatorId) {
        return tournamentRepository.findByCreatorId(creatorId).stream()
                .map(tournamentMapper::toResponse)
                .toList();
    }

    @Transactional
    public TournamentResponse cancelTournament(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));
        tournament.setStatus(TournamentStatus.CANCELLED);
        return tournamentMapper.toResponse(tournamentRepository.save(tournament));
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void markDueTournamentsAsInProgress() {
        List<Tournament> tournaments = tournamentRepository.findByStatusInAndStartDateBefore(
                List.of(TournamentStatus.UPCOMING, TournamentStatus.REGISTRATION_OPEN),
                LocalDateTime.now());

        tournaments.forEach(tournament -> tournament.setStatus(TournamentStatus.IN_PROGRESS));
        tournamentRepository.saveAll(tournaments);
    }
}

