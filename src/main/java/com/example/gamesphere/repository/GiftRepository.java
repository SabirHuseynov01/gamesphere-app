package com.example.gamesphere.repository;

import com.example.gamesphere.entity.Gift;
import com.example.gamesphere.enums.GiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Long> {

    List<Gift> findBySenderIdOrderByCreatedAtDesc(Long senderId);

    List<Gift> findByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(String recipientEmail);

    Optional<Gift> findByClaimToken(String claimToken);

    boolean existsByClaimToken(String claimToken);

    List<Gift> findByStatusAndExpiresAtBefore(GiftStatus status, LocalDateTime now);
}
