package com.example.gamesphere.repository;

import com.example.gamesphere.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findPageByProductIdAndApprovedTrue(Long productId, Pageable pageable);
    List<Review> findByProductIdAndApprovedTrue(Long productId);
    boolean existsByProductIdAndUserId(Long productId, Long userId);
    Page<Review> findPageByGameIdAndApprovedTrue(Long gameId, Pageable pageable);
    List<Review> findByGameIdAndApprovedTrue(Long gameId);
    boolean existsByGameIdAndUserId(Long gameId, Long userId);
}
