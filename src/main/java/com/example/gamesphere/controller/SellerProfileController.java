package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.SellerProfileRequest;
import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.SellerProfileResponse;
import com.example.gamesphere.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@Tag(name = "Seller", description = "Seller profile operations")
@SecurityRequirement(name = "bearerAuth")
public class SellerProfileController {

    private final SellerService sellerService;

    @PostMapping("/profile")
    @Operation(summary = "Create seller profile", description = "Creates a seller profile for the current user.")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> createSellerProfile(
            @Valid @RequestBody SellerProfileRequest request) {
        SellerProfileResponse profile = sellerService.createSellerProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seller profile created", profile));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get seller profile", description = "Returns current user's seller profile.")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> getSellerProfile() {
        SellerProfileResponse profile = sellerService.getCurrentSellerProfile();
        return ResponseEntity.ok(ApiResponse.success("Seller profile retrieved", profile));
    }
}
