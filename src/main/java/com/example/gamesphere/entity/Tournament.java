package com.example.gamesphere.entity;

import com.example.gamesphere.enums.TournamentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tournament extends BaseEntity{

    @Column(nullable = false)
    private String title;

    @Column(length = 1500)
    private String description;

    @Column(nullable = false)
    private BigDecimal entryFee;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    private String game;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TournamentStatus status = TournamentStatus.UPCOMING;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    private int maxParticipants;

    @Builder.Default
    private int currentParticipants = 0;

    private BigDecimal prizePool;

}
