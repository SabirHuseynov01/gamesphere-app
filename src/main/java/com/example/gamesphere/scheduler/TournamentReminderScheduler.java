package com.example.gamesphere.scheduler;

import com.example.gamesphere.entity.TournamentParticipant;
import com.example.gamesphere.enums.TournamentParticipantStatus;
import com.example.gamesphere.enums.TournamentStatus;
import com.example.gamesphere.repository.TournamentParticipantRepository;
import com.example.gamesphere.service.EmailService;
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
public class TournamentReminderScheduler {

    private final TournamentParticipantRepository tournamentParticipantRepository;
    private final EmailService emailService;

    @Transactional
    @Scheduled(fixedRate = 900000)
    public void sendTournamentStartReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderWindowEnd = now.plusHours(1);

        List<TournamentParticipant> participants = tournamentParticipantRepository
                .findParticipantsForTournamentReminder(
                        TournamentParticipantStatus.REGISTERED,
                        List.of(TournamentStatus.UPCOMING, TournamentStatus.REGISTRATION_OPEN),
                        now,
                        reminderWindowEnd
                );

        for (TournamentParticipant participant : participants) {
            emailService.sendTournamentReminderEmail(participant.getTournament(), participant.getUser());
            participant.setReminderEmailSent(true);
        }

        tournamentParticipantRepository.saveAll(participants);
        log.info("Tournament reminder emails processed for {} participants", participants.size());
    }
}
