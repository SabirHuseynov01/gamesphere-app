package com.example.gamesphere.dto.response;

import com.example.gamesphere.enums.TournamentParticipantStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentParticipantResponse {

    private Long id;
    private Long tournamentId;
    private Long userId;
    private String username;
    private String inGameNickname;
    private String teamName;
    private TournamentParticipantStatus status;
}
