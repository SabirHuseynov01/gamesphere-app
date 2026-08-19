package com.example.gamesphere.repository;

import com.example.gamesphere.entity.TournamentParticipant;
import com.example.gamesphere.enums.TournamentParticipantStatus;
import com.example.gamesphere.enums.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, Long> {

    List<TournamentParticipant> findByTournamentId(Long tournamentId);
    @Query("""
            select participant
            from TournamentParticipant participant
            join fetch participant.user
            join fetch participant.tournament tournament
            where participant.reminderEmailSent = false
              and participant.status = :participantStatus
              and tournament.status in :tournamentStatuses
              and tournament.startDate between :from and :to
            """)
    List<TournamentParticipant> findParticipantsForTournamentReminder(
            @Param("participantStatus") TournamentParticipantStatus participantStatus,
            @Param("tournamentStatuses") List<TournamentStatus> tournamentStatuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
    Optional<TournamentParticipant> findByTournamentIdAndUserId(Long tournamentId, Long userId);
    boolean existsByTournamentIdAndUserId(Long tournamentId, Long userId);
    long countByTournamentId(Long tournamentId);
}
