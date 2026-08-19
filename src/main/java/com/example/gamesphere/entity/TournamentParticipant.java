package com.example.gamesphere.entity;

import com.example.gamesphere.enums.TournamentParticipantStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tournament_participants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentParticipant extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String inGameNickname;
    private String teamName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TournamentParticipantStatus status = TournamentParticipantStatus.REGISTERED;

    @Builder.Default
    private boolean reminderEmailSent = false;
}
