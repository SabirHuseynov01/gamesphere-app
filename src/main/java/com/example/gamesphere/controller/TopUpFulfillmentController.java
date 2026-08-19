package com.example.gamesphere.controller;

import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.TopUpFulfillmentResponse;
import com.example.gamesphere.service.TopUpFulfillmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/top-up-fulfillments")
@RequiredArgsConstructor
@Tag(name = "Top-up Fulfillments", description = "Current user's player account top-up delivery status")
@SecurityRequirement(name = "bearerAuth")
public class TopUpFulfillmentController {

    private final TopUpFulfillmentService fulfillmentService;

    @GetMapping("/{id}")
    @Operation(summary = "Get top-up fulfillment", description = "Returns a fulfillment owned by the current user.")
    public ResponseEntity<ApiResponse<TopUpFulfillmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Top-up fulfillment retrieved",
                fulfillmentService.getCurrentUserFulfillment(id)));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get order top-ups", description = "Lists top-up fulfillments for the current user's order.")
    public ResponseEntity<ApiResponse<List<TopUpFulfillmentResponse>>> getByOrder(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order top-up fulfillments retrieved",
                fulfillmentService.getCurrentUserFulfillmentsByOrder(orderId)));
    }
}
