package com.example.gamesphere.repository;

import com.example.gamesphere.entity.Notification;
import com.example.gamesphere.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserEmail(String email, Pageable pageable);
    long countByUserEmailAndStatus(String email, NotificationStatus status);
}
