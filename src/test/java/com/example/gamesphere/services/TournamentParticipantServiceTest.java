package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.TournamentJoinRequest;
import com.example.gamesphere.dto.response.TournamentParticipantResponse;
import com.example.gamesphere.entity.Tournament;
import com.example.gamesphere.entity.TournamentParticipant;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.TournamentParticipantStatus;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.repository.TournamentParticipantRepository;
import com.example.gamesphere.repository.TournamentRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.TournamentParticipantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TournamentParticipantServiceTest extends ServiceTestSupport {

    @Mock TournamentParticipantRepository participantRepository;
    @Mock TournamentRepository tournamentRepository;
    @Mock UserRepository userRepository;
    @InjectMocks
    TournamentParticipantService participantService;

    @Test
    void joinTournamentRegistersCurrentUserAndUpdatesCount() {
        authenticate("player@mail.com");
        User user = user(2L, "player@mail.com");
        Tournament tournament = withId(new Tournament(), 10L);
        tournament.setMaxParticipants(16);
        TournamentJoinRequest request = new TournamentJoinRequest("SabirPro", "Gamesphere");
        when(userRepository.findByEmail("player@mail.com")).thenReturn(Optional.of(user));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(participantRepository.existsByTournamentIdAndUserId(10L, 2L)).thenReturn(false);
        when(participantRepository.countByTournamentId(10L)).thenReturn(3L);
        when(participantRepository.save(org.mockito.ArgumentMatchers.any(TournamentParticipant.class)))
                .thenAnswer(invocation -> {
                    TournamentParticipant participant = invocation.getArgument(0);
                    participant.setId(20L);
                    return participant;
                });

        TournamentParticipantResponse response = participantService.joinTournament(10L, request);

        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getStatus()).isEqualTo(TournamentParticipantStatus.REGISTERED);
        assertThat(tournament.getCurrentParticipants()).isEqualTo(4);
    }

    @Test
    void fullTournamentRejectsNewParticipant() {
        authenticate("player@mail.com");
        User user = user(2L, "player@mail.com");
        Tournament tournament = withId(new Tournament(), 10L);
        tournament.setMaxParticipants(4);
        when(userRepository.findByEmail("player@mail.com")).thenReturn(Optional.of(user));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(participantRepository.existsByTournamentIdAndUserId(10L, 2L)).thenReturn(false);
        when(participantRepository.countByTournamentId(10L)).thenReturn(4L);

        assertThatThrownBy(() -> participantService.joinTournament(
                10L, new TournamentJoinRequest("player", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("limit is full");
    }
}

