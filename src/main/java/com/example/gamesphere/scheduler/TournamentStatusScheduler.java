package com.example.gamesphere.scheduler;

import com.example.gamesphere.entity.Tournament;
import com.example.gamesphere.enums.TournamentStatus;
import com.example.gamesphere.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TournamentStatusScheduler {

    private final TournamentRepository tournamentRepository;

    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void updateTournamentStatuses() {
        LocalDateTime now = LocalDateTime.now();

        List<Tournament> tournamentsToStart = tournamentRepository.findByStatusInAndStartDateBefore(
                List.of(TournamentStatus.UPCOMING, TournamentStatus.REGISTRATION_OPEN),
                now);

        List<Tournament> startedTournaments = tournamentsToStart.stream()
                .filter(tournament -> tournament.getEndDate().isAfter(now))
                .toList();

        startedTournaments.forEach(tournament -> tournament.setStatus(TournamentStatus.IN_PROGRESS));

        List<Tournament> tournamentsToFinish = tournamentRepository.findByStatusInAndEndDateBefore(
                List.of(TournamentStatus.UPCOMING, TournamentStatus.REGISTRATION_OPEN, TournamentStatus.IN_PROGRESS),
                now);

        tournamentsToFinish.forEach(tournament -> tournament.setStatus(TournamentStatus.FINISHED));

        if (!startedTournaments.isEmpty() || !tournamentsToFinish.isEmpty()) {
            log.info("Tournament status scheduler updated {} starting and {} finishing tournaments",
                    startedTournaments.size(),
                    tournamentsToFinish.size());
        }
    }
}
