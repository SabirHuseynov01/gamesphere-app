package com.example.gamesphere.service;

import com.example.gamesphere.dto.response.NotificationResponse;
import com.example.gamesphere.entity.Notification;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.NotificationStatus;
import com.example.gamesphere.enums.NotificationType;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.NotificationMapper;
import com.example.gamesphere.repository.NotificationRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;


    @Transactional
    public NotificationResponse createNotification(User user, NotificationType type, String message, String title) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setTitle(title);
        notification.setStatus(NotificationStatus.UNREAD);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    public List<NotificationResponse> getCurrentUserNotifications(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return notificationRepository.findByUserEmail(email, pageable).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    public long countUnreadNotifications() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return notificationRepository.countByUserEmailAndStatus(email, NotificationStatus.UNREAD);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        User user = getCurrentUser();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found");
        }

        notification.setStatus(NotificationStatus.READ);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

}
