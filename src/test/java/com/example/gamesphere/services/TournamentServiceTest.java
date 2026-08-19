package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.TournamentCreateRequest;
import com.example.gamesphere.dto.response.TournamentResponse;
import com.example.gamesphere.entity.Tournament;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.TournamentStatus;
import com.example.gamesphere.mapper.TournamentMapper;
import com.example.gamesphere.repository.TournamentRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.TournamentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest extends ServiceTestSupport {

    @Mock TournamentRepository tournamentRepository;
    @Mock TournamentMapper tournamentMapper;
    @Mock UserRepository userRepository;
    @InjectMocks TournamentService tournamentService;

    @Test
    void createTournamentAssignsAuthenticatedUserAsCreator() {
        authenticate("creator@mail.com");
        User creator = user(9L, "creator@mail.com");
        TournamentCreateRequest request = new TournamentCreateRequest();
        Tournament tournament = new Tournament();
        TournamentResponse expected = new TournamentResponse();
        when(tournamentMapper.toEntity(request)).thenReturn(tournament);
        when(userRepository.findByEmail("creator@mail.com")).thenReturn(Optional.of(creator));
        when(tournamentRepository.save(tournament)).thenReturn(tournament);
        when(tournamentMapper.toResponse(tournament)).thenReturn(expected);

        assertThat(tournamentService.createTournament(request)).isSameAs(expected);
        assertThat(tournament.getCreator()).isSameAs(creator);
    }

    @Test
    void cancelTournamentChangesStatusAndPersists() {
        Tournament tournament = new Tournament();
        tournament.setStatus(TournamentStatus.UPCOMING);
        TournamentResponse expected = new TournamentResponse();
        when(tournamentRepository.findById(4L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(tournament)).thenReturn(tournament);
        when(tournamentMapper.toResponse(tournament)).thenReturn(expected);

        assertThat(tournamentService.cancelTournament(4L)).isSameAs(expected);
        assertThat(tournament.getStatus()).isEqualTo(TournamentStatus.CANCELLED);
    }

    @Test
    void schedulerMarksDueTournamentsInProgress() {
        Tournament tournament = new Tournament();
        tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);
        tournament.setStartDate(LocalDateTime.now().minusMinutes(1));
        when(tournamentRepository.findByStatusInAndStartDateBefore(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(tournament));

        tournamentService.markDueTournamentsAsInProgress();

        assertThat(tournament.getStatus()).isEqualTo(TournamentStatus.IN_PROGRESS);
        verify(tournamentRepository).saveAll(List.of(tournament));
    }
}
