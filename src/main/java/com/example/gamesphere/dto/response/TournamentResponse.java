package com.example.gamesphere.dto.response;

import com.example.gamesphere.enums.TournamentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal entryFee;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String game;
    private TournamentStatus status;
    private int maxParticipants;
    private int currentParticipants;
    private BigDecimal prizePool;
    private Long creatorId;
    private String creatorUsername;
}
