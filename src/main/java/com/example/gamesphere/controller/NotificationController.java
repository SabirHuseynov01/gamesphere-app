package com.example.gamesphere.controller;

import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.NotificationResponse;
import com.example.gamesphere.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Current user's notification inbox")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get my notifications", description = "Returns paginated notifications for current user.")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(Pageable pageable) {
        List<NotificationResponse> notifications = notificationService.getCurrentUserNotifications(pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notifications));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Count unread notifications", description = "Returns unread notification count for current user.")
    public ResponseEntity<ApiResponse<Long>> countUnreadNotifications() {
        return ResponseEntity.ok(ApiResponse.success("Unread notifications counted", notificationService.countUnreadNotifications()));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a notification as read for current user.")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id) {
        NotificationResponse notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }
}
