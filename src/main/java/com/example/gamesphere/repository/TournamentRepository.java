package com.example.gamesphere.repository;

import com.example.gamesphere.entity.Tournament;
import com.example.gamesphere.enums.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament> findByStatus(TournamentStatus status);

    List<Tournament> findByGame(String game);

    List<Tournament> findByStartDateAfter(LocalDateTime date);

    List<Tournament> findByCreatorId(Long creatorId);

    List<Tournament> findByStatusInAndStartDateBefore(
            List<TournamentStatus> statuses, LocalDateTime now);

    List<Tournament> findByStatusInAndEndDateBefore(
            List<TournamentStatus> statuses, LocalDateTime now);
}
