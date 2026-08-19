package com.example.gamesphere.services;

import com.example.gamesphere.dto.response.NotificationResponse;
import com.example.gamesphere.entity.Notification;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.NotificationStatus;
import com.example.gamesphere.enums.NotificationType;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.NotificationMapper;
import com.example.gamesphere.repository.NotificationRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest extends ServiceTestSupport {

    @Mock NotificationRepository notificationRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationMapper notificationMapper;
    @InjectMocks
    NotificationService notificationService;

    @Test
    void createNotificationAlwaysStartsUnread() {
        User user = user(1L, "user@mail.com");
        NotificationResponse expected = new NotificationResponse();
        when(notificationRepository.save(org.mockito.ArgumentMatchers.any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationMapper.toResponse(org.mockito.ArgumentMatchers.any(Notification.class)))
                .thenReturn(expected);

        assertThat(notificationService.createNotification(
                user, NotificationType.PAYMENT_SUCCESS, "Paid", "Payment complete"))
                .isSameAs(expected);
    }

    @Test
    void userCannotReadAnotherUsersNotification() {
        authenticate("user@mail.com");
        User currentUser = user(1L, "user@mail.com");
        Notification notification = new Notification();
        notification.setUser(user(2L, "other@mail.com"));
        notification.setStatus(NotificationStatus.UNREAD);
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(currentUser));
        when(notificationRepository.findById(4L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(4L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Notification not found");
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);
    }
}

