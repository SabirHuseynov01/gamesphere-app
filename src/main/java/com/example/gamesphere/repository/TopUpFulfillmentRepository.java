package com.example.gamesphere.repository;

import com.example.gamesphere.entity.TopUpFulfillment;
import com.example.gamesphere.enums.TopUpFulfillmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopUpFulfillmentRepository extends JpaRepository<TopUpFulfillment, Long> {

    Optional<TopUpFulfillment> findByOrderItemId(Long orderItemId);

    List<TopUpFulfillment> findAllByOrderItemOrderIdOrderByIdAsc(Long orderId);

    Page<TopUpFulfillment> findAllByStatus(TopUpFulfillmentStatus status, Pageable pageable);
}
