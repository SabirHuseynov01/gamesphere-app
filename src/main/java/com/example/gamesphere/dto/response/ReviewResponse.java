package com.example.gamesphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long productId;
    private Long gameId;
    private String username;
    private Double rating;
    private String comment;
    private boolean approved;
    private LocalDateTime createdAt;
}
