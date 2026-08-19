package com.example.gamesphere.repository;

import com.example.gamesphere.entity.DigitalCode;
import com.example.gamesphere.enums.DigitalCodeStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DigitalCodeRepository extends JpaRepository<DigitalCode, Long> {

    boolean existsByCodeValue(String codeValue);

    long countByProductIdAndStatus(Long productId, DigitalCodeStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select code from DigitalCode code
             where code.product.id = :productId
               and code.status = :status
             order by code.id
            """)
    List<DigitalCode> findAvailableForUpdate(@Param("productId") Long productId,
                                             @Param("status") DigitalCodeStatus status,
                                             Pageable pageable);
}
