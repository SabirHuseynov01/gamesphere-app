package com.example.gamesphere.repository;

import com.example.gamesphere.entity.UserEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserEntitlementRepository extends JpaRepository<UserEntitlement, Long> {

    List<UserEntitlement> findAllByUserIdOrderByGrantedAtDesc(Long userId);

    Optional<UserEntitlement> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
