package com.example.gamesphere.dto.response;

import com.example.gamesphere.enums.NotificationStatus;
import com.example.gamesphere.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private NotificationStatus status;
    private String title;
    private String message;
    private LocalDateTime createdAt;
}
