package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.TournamentJoinRequest;
import com.example.gamesphere.dto.response.TournamentParticipantResponse;
import com.example.gamesphere.entity.Tournament;
import com.example.gamesphere.entity.TournamentParticipant;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.TournamentParticipantStatus;
import com.example.gamesphere.enums.TournamentStatus;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TournamentParticipantService {

    private final TournamentParticipantRepository participantRepository;
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;

    /** Registration is open until the bracket starts; after that the roster is fixed. */
    private static final Set<TournamentStatus> JOINABLE_STATUSES =
            Set.of(TournamentStatus.UPCOMING, TournamentStatus.REGISTRATION_OPEN);

    @Transactional
    public TournamentParticipantResponse joinTournament(Long tournamentId, TournamentJoinRequest request) {
        User user = getCurrentUser();
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        if (!JOINABLE_STATUSES.contains(tournament.getStatus())) {
            throw new BusinessException("Registration is closed for this tournament.");
        }

        TournamentParticipant participant = participantRepository
                .findByTournamentIdAndUserId(tournamentId, user.getId())
                .orElse(null);

        if (participant != null && participant.getStatus() != TournamentParticipantStatus.LEFT) {
            throw new BusinessException(participant.getStatus() == TournamentParticipantStatus.DISQUALIFIED
                    ? "You have been disqualified from this tournament."
                    : "You have already joined this tournament.");
        }

        long activeCount = countActive(tournamentId);
        if (tournament.getMaxParticipants() > 0 && activeCount >= tournament.getMaxParticipants()) {
            throw new BusinessException("Tournament participant limit is full.");
        }

        // (tournament_id, user_id) is unique, so a player who left and comes back
        // gets their old row back rather than a second one the database would refuse.
        if (participant == null) {
            participant = new TournamentParticipant();
            participant.setTournament(tournament);
            participant.setUser(user);
        }

        participant.setInGameNickname(request.getInGameNickname());
        participant.setTeamName(request.getTeamName());
        participant.setStatus(TournamentParticipantStatus.REGISTERED);

        tournament.setCurrentParticipants((int) activeCount + 1);
        tournamentRepository.save(tournament);

        return toResponse(participantRepository.save(participant));
    }

    @Transactional
    public void leaveTournament(Long tournamentId) {
        User user = getCurrentUser();
        // A row that already says LEFT is not a membership; leaving twice must not
        // count the same player out a second time.
        TournamentParticipant participant = findActive(tournamentId, user.getId());

        Tournament tournament = participant.getTournament();
        long activeCount = countActive(tournamentId);

        participant.setStatus(TournamentParticipantStatus.LEFT);
        // Recounted rather than decremented, so a stored figure that drifted
        // before this fix settles back on the real roster.
        tournament.setCurrentParticipants((int) Math.max(0, activeCount - 1));

        participantRepository.save(participant);
        tournamentRepository.save(tournament);
    }

    /** People who left are history, not the roster — DISQUALIFIED stays visible. */
    @Transactional(readOnly = true)
    public List<TournamentParticipantResponse> getParticipants(Long tournamentId) {
        return participantRepository
                .findByTournamentIdAndStatusNot(tournamentId, TournamentParticipantStatus.LEFT)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TournamentParticipantResponse getCurrentUserParticipation(Long tournamentId) {
        User user = getCurrentUser();
        return toResponse(findActive(tournamentId, user.getId()));
    }

    @Transactional
    public TournamentParticipantResponse disqualifyParticipant(Long tournamentId, Long userId) {
        TournamentParticipant participant = participantRepository.findByTournamentIdAndUserId(tournamentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament participant not found"));
        participant.setStatus(TournamentParticipantStatus.DISQUALIFIED);
        return toResponse(participantRepository.save(participant));
    }

    private TournamentParticipant findActive(Long tournamentId, Long userId) {
        return participantRepository.findByTournamentIdAndUserId(tournamentId, userId)
                .filter(participant -> participant.getStatus() != TournamentParticipantStatus.LEFT)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament participant not found"));
    }

    private long countActive(Long tournamentId) {
        return participantRepository.countByTournamentIdAndStatusNot(
                tournamentId, TournamentParticipantStatus.LEFT);
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
