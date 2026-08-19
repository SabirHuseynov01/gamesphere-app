package com.example.gamesphere.entity;

import com.example.gamesphere.enums.DigitalCodeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "digital_codes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalCode extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "code_value", nullable = false, unique = true, length = 500)
    private String codeValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DigitalCodeStatus status = DigitalCodeStatus.AVAILABLE;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}
