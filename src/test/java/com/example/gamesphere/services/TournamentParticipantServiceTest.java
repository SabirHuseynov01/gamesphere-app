package com.example.gamesphere.services;

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
import com.example.gamesphere.service.TournamentParticipantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TournamentParticipantServiceTest extends ServiceTestSupport {

    private static final TournamentParticipantStatus LEFT = TournamentParticipantStatus.LEFT;

    @Mock TournamentParticipantRepository participantRepository;
    @Mock TournamentRepository tournamentRepository;
    @Mock UserRepository userRepository;
    @InjectMocks
    TournamentParticipantService participantService;

    @Test
    void joinTournamentRegistersCurrentUserAndUpdatesCount() {
        User user = signedIn();
        Tournament tournament = tournament(16);
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(participantRepository.findByTournamentIdAndUserId(10L, 2L)).thenReturn(Optional.empty());
        when(participantRepository.countByTournamentIdAndStatusNot(10L, LEFT)).thenReturn(3L);
        when(participantRepository.save(any(TournamentParticipant.class)))
                .thenAnswer(invocation -> {
                    TournamentParticipant participant = invocation.getArgument(0);
                    participant.setId(20L);
                    return participant;
                });

        TournamentParticipantResponse response = participantService.joinTournament(
                10L, new TournamentJoinRequest("SabirPro", "Gamesphere"));

        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getStatus()).isEqualTo(TournamentParticipantStatus.REGISTERED);
        assertThat(tournament.getCurrentParticipants()).isEqualTo(4);
    }

    @Test
    void fullTournamentRejectsNewParticipant() {
        signedIn();
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament(4)));
        when(participantRepository.findByTournamentIdAndUserId(10L, 2L)).thenReturn(Optional.empty());
        when(participantRepository.countByTournamentIdAndStatusNot(10L, LEFT)).thenReturn(4L);

        assertThatThrownBy(() -> participantService.joinTournament(
                10L, new TournamentJoinRequest("player", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("limit is full");
    }

    @Test
    void playerWhoLeftCanComeBackOnTheSameRow() {
        User user = signedIn();
        Tournament tournament = tournament(16);
        TournamentParticipant previous = participant(tournament, user, LEFT);
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(participantRepository.findByTournamentIdAndUserId(10L, 2L)).thenReturn(Optional.of(previous));
        when(participantRepository.countByTournamentIdAndStatusNot(10L, LEFT)).thenReturn(5L);
        when(participantRepository.save(any(TournamentParticipant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TournamentParticipantResponse response = participantService.joinTournament(
                10L, new TournamentJoinRequest("NewNick", "NewClan"));

        // The unique (tournament_id, user_id) key means a fresh insert would fail,
        // so the old row is revived with the new details.
        assertThat(response.getId()).isEqualTo(previous.getId());
        assertThat(response.getStatus()).isEqualTo(TournamentParticipantStatus.REGISTERED);
        assertThat(response.getInGameNickname()).isEqualTo("NewNick");
        assertThat(tournament.getCurrentParticipants()).isEqualTo(6);
    }

    @Test
    void activeParticipantCannotJoinTwice() {
        User user = signedIn();
        Tournament tournament = tournament(16);
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(participantRepository.findByTournamentIdAndUserId(10L, 2L))
                .thenReturn(Optional.of(participant(tournament, user, TournamentParticipantStatus.REGISTERED)));

        assertThatThrownBy(() -> participantService.joinTournament(
                10L, new TournamentJoinRequest("again", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already joined");
    }

    @Test
    void startedTournamentRejectsRegistration() {
        signedIn();
        Tournament tournament = tournament(16);
        tournament.setStatus(TournamentStatus.FINISHED);
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() -> participantService.joinTournament(
                10L, new TournamentJoinRequest("late", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("closed");
        verify(participantRepository, never()).save(any());
    }

    @Test
    void leavingRecountsTheRosterAndMarksTheRowLeft() {
        User user = signedIn();
        Tournament tournament = tournament(16);
        // A stored count that drifted away from the real roster.
        tournament.setCurrentParticipants(9);
        TournamentParticipant participant = participant(tournament, user, TournamentParticipantStatus.REGISTERED);
        when(participantRepository.findByTournamentIdAndUserId(10L, 2L)).thenReturn(Optional.of(participant));
        when(participantRepository.countByTournamentIdAndStatusNot(10L, LEFT)).thenReturn(3L);

        participantService.leaveTournament(10L);

        assertThat(participant.getStatus()).isEqualTo(LEFT);
        assertThat(tournament.getCurrentParticipants()).isEqualTo(2);
    }

    @Test
    void leavingTwiceIsRefused() {
        User user = signedIn();
        Tournament tournament = tournament(16);
        when(participantRepository.findByTournamentIdAndUserId(10L, 2L))
                .thenReturn(Optional.of(participant(tournament, user, LEFT)));

        assertThatThrownBy(() -> participantService.leaveTournament(10L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(tournamentRepository, never()).save(any());
    }

    @Test
    void rosterLeavesOutPlayersWhoLeft() {
        Tournament tournament = tournament(16);
        TournamentParticipant stayed = participant(tournament, user(3L, "stayed@mail.com"),
                TournamentParticipantStatus.REGISTERED);
        when(participantRepository.findByTournamentIdAndStatusNot(10L, LEFT)).thenReturn(List.of(stayed));

        List<TournamentParticipantResponse> roster = participantService.getParticipants(10L);

        assertThat(roster).extracting(TournamentParticipantResponse::getUserId).containsExactly(3L);
    }

    @Test
    void playerWhoLeftIsNoLongerReportedAsParticipating() {
        User user = signedIn();
        when(participantRepository.findByTournamentIdAndUserId(10L, 2L))
                .thenReturn(Optional.of(participant(tournament(16), user, LEFT)));

        assertThatThrownBy(() -> participantService.getCurrentUserParticipation(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private User signedIn() {
        authenticate("player@mail.com");
        User user = user(2L, "player@mail.com");
        when(userRepository.findByEmail("player@mail.com")).thenReturn(Optional.of(user));
        return user;
    }

    private Tournament tournament(int maxParticipants) {
        Tournament tournament = withId(new Tournament(), 10L);
        tournament.setMaxParticipants(maxParticipants);
        return tournament;
    }

    private TournamentParticipant participant(Tournament tournament, User user, TournamentParticipantStatus status) {
        TournamentParticipant participant = withId(new TournamentParticipant(), 30L + user.getId());
        participant.setTournament(tournament);
        participant.setUser(user);
        participant.setInGameNickname("old-" + user.getId());
        participant.setStatus(status);
        return participant;
    }
}
