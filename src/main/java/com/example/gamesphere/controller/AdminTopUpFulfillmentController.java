package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.UpdateTopUpFulfillmentRequest;
import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.PageResponse;
import com.example.gamesphere.dto.response.TopUpFulfillmentResponse;
import com.example.gamesphere.enums.TopUpFulfillmentStatus;
import com.example.gamesphere.service.TopUpFulfillmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/top-up-fulfillments")
@RequiredArgsConstructor
@Tag(name = "Admin Top-up Fulfillments", description = "Administrative top-up delivery processing")
@SecurityRequirement(name = "bearerAuth")
public class AdminTopUpFulfillmentController {

    private final TopUpFulfillmentService fulfillmentService;

    @GetMapping
    @Operation(summary = "List top-up fulfillments", description = "Lists all fulfillments, optionally filtered by status.")
    public ResponseEntity<ApiResponse<PageResponse<TopUpFulfillmentResponse>>> getAll(
            @RequestParam(required = false) TopUpFulfillmentStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Top-up fulfillments retrieved",
                fulfillmentService.getAll(status, pageable)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update top-up status", description = "Processes, completes, fails or cancels a top-up fulfillment.")
    public ResponseEntity<ApiResponse<TopUpFulfillmentResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTopUpFulfillmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Top-up fulfillment status updated",
                fulfillmentService.updateStatus(id, request)));
    }
}
