package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.UserProfileUpdateRequest;
import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.UserProfileResponse;
import com.example.gamesphere.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Current user profile operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get my profile", description = "Returns current authenticated user's profile.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUserProfile() {
        UserProfileResponse profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", profile));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update my profile", description = "Updates current authenticated user's profile.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse profile = userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }
}
