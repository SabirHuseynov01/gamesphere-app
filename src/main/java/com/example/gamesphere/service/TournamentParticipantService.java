package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.TournamentJoinRequest;
import com.example.gamesphere.dto.response.TournamentParticipantResponse;
import com.example.gamesphere.entity.Tournament;
import com.example.gamesphere.entity.TournamentParticipant;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.TournamentParticipantStatus;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.repository.TournamentParticipantRepository;
import com.example.gamesphere.repository.TournamentRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentParticipantService {

    private final TournamentParticipantRepository participantRepository;
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;

    @Transactional
    public TournamentParticipantResponse joinTournament(Long tournamentId, TournamentJoinRequest request) {
        User user = getCurrentUser();
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        if (participantRepository.existsByTournamentIdAndUserId(tournamentId, user.getId())) {
            throw new BusinessException("You have already joined this tournament.");
        }

        long participantCount = participantRepository.countByTournamentId(tournamentId);
        if (tournament.getMaxParticipants() > 0 && participantCount >= tournament.getMaxParticipants()) {
            throw new BusinessException("Tournament participant limit is full.");
        }

        TournamentParticipant participant = new TournamentParticipant();
        participant.setTournament(tournament);
        participant.setUser(user);
        participant.setInGameNickname(request.getInGameNickname());
        participant.setTeamName(request.getTeamName());
        participant.setStatus(TournamentParticipantStatus.REGISTERED);

        tournament.setCurrentParticipants((int) participantCount + 1);
        tournamentRepository.save(tournament);

        return toResponse(participantRepository.save(participant));
    }

    @Transactional
    public void leaveTournament(Long tournamentId) {
        User user = getCurrentUser();
        TournamentParticipant participant = participantRepository.findByTournamentIdAndUserId(tournamentId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament participant not found"));

        Tournament tournament = participant.getTournament();
        tournament.setCurrentParticipants(Math.max(0, tournament.getCurrentParticipants() - 1));

        participant.setStatus(TournamentParticipantStatus.LEFT);
        participantRepository.save(participant);
        tournamentRepository.save(tournament);
    }

    @Transactional(readOnly = true)
    public List<TournamentParticipantResponse> getParticipants(Long tournamentId) {
        return participantRepository.findByTournamentId(tournamentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TournamentParticipantResponse getCurrentUserParticipation(Long tournamentId) {
        User user = getCurrentUser();
        TournamentParticipant participant = participantRepository.findByTournamentIdAndUserId(tournamentId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament participant not found"));
        return toResponse(participant);
    }

    @Transactional
    public TournamentParticipantResponse disqualifyParticipant(Long tournamentId, Long userId) {
        TournamentParticipant participant = participantRepository.findByTournamentIdAndUserId(tournamentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament participant not found"));
        participant.setStatus(TournamentParticipantStatus.DISQUALIFIED);
        return toResponse(participantRepository.save(participant));
    }

    private TournamentParticipantResponse toResponse(TournamentParticipant participant) {
        return new TournamentParticipantResponse(
                participant.getId(),
                participant.getTournament().getId(),
                participant.getUser().getId(),
                participant.getUser().getUsername(),
                participant.getInGameNickname(),
                participant.getTeamName(),
                participant.getStatus());
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
