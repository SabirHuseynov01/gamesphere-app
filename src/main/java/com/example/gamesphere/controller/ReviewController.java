package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.ReviewCreateRequest;
import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.PageResponse;
import com.example.gamesphere.dto.response.ReviewResponse;
import com.example.gamesphere.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product review operations")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create review", description = "Creates a product review for the current user.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        ReviewResponse review = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review created successfully", review));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get product reviews", description = "Returns paginated reviews for a selected product.")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId, Pageable pageable) {

        PageResponse<ReviewResponse> reviews = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Product reviews retrieved", reviews));
    }

    @GetMapping("/product/{productId}/approved")
    @Operation(summary = "Get approved product reviews", description = "Returns approved reviews for a selected product.")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getApprovedProductReviews(@PathVariable Long productId) {
        List<ReviewResponse> reviews = reviewService.getApprovedProductReviews(productId);
        return ResponseEntity.ok(ApiResponse.success("Approved product reviews retrieved", reviews));
    }

    @GetMapping("/game/{gameId}")
    @Operation(summary = "Get game reviews", description = "Returns paginated approved reviews for a game.")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getGameReviews(
            @PathVariable Long gameId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Game reviews retrieved",
                reviewService.getGameReviews(gameId, pageable)));
    }

    @GetMapping("/game/{gameId}/approved")
    @Operation(summary = "Get approved game reviews", description = "Returns approved reviews for a game.")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getApprovedGameReviews(@PathVariable Long gameId) {
        return ResponseEntity.ok(ApiResponse.success("Approved game reviews retrieved",
                reviewService.getApprovedGameReviews(gameId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete review", description = "Deletes the current user's review.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}
