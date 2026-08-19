package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.TestEmailRequest;
import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/email")
@RequiredArgsConstructor
@Tag(name = "Admin Email", description = "SMTP delivery verification")
@SecurityRequirement(name = "bearerAuth")
public class AdminEmailController {

    private final EmailService emailService;

    @PostMapping("/test")
    @Operation(summary = "Send SMTP test email")
    public ResponseEntity<ApiResponse<Void>> sendTestEmail(
            @Valid @RequestBody TestEmailRequest request) {
        emailService.sendTestEmail(request.getTo());
        return ResponseEntity.ok(ApiResponse.success("Test email sent", null));
    }
}
