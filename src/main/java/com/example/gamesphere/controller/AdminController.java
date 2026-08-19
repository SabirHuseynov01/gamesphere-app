package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.OrderStatusUpdateRequest;
import com.example.gamesphere.dto.response.*;
import com.example.gamesphere.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative operations for users, orders, reviews, sellers, products and tournaments")
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final SellerService sellerService;
    private final ProductService productService;
    private final TournamentService tournamentService;
    private final TournamentParticipantService tournamentParticipantService;

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by id", description = "Returns a user's profile for admin inspection.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", userService.getUserProfile(id)));
    }

    @GetMapping("/users/username/{username}")
    @Operation(summary = "Get user by username", description = "Returns a user's profile by username.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfileByUsername(@PathVariable String username) {
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved",
                userService.getUserProfileByUsername(username)));
    }

    @PatchMapping("/users/{id}/balance")
    @Operation(summary = "Update user balance", description = "Adds or subtracts balance for a selected user.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateBalance(
            @PathVariable Long id,
            @RequestParam Double amount) {
        return ResponseEntity.ok(ApiResponse.success("Balance updated", userService.updateBalance(id, amount)));
    }

    @GetMapping("/users/{id}/exists")
    @Operation(summary = "Check user existence", description = "Checks whether a user exists by id.")
    public ResponseEntity<ApiResponse<Boolean>> existsById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User existence checked", userService.existsById(id)));
    }

    @PatchMapping("/orders/{id}/status")
    @Operation(summary = "Update order status", description = "Updates order status from the admin panel.")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated",
                orderService.updateOrderStatus(id, request)));
    }

    @GetMapping("/orders/user/{userId}")
    @Operation(summary = "Get user orders", description = "Lists all orders belonging to a selected user.")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("User orders retrieved",
                orderService.getOrdersByUserId(userId)));
    }

    @PatchMapping("/reviews/{id}/approve")
    @Operation(summary = "Approve review", description = "Approves a product review for public display.")
    public ResponseEntity<ApiResponse<ReviewResponse>> approveReview(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Review approved", reviewService.approveReview(id)));
    }

    @PatchMapping("/sellers/{id}/approve")
    @Operation(summary = "Approve seller", description = "Approves a seller profile.")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> approveSeller(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Seller approved",
                sellerService.approveSellerProfile(id)));
    }

    @PatchMapping("/products/{id}/deactivate")
    @Operation(summary = "Deactivate product", description = "Marks a product inactive from the admin panel.")
    public ResponseEntity<ApiResponse<ProductResponse>> deactivateProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Product deactivated",
                productService.deactivateProduct(id)));
    }

    @PatchMapping("/tournaments/{id}/cancel")
    @Operation(summary = "Cancel tournament", description = "Cancels an active or upcoming tournament.")
    public ResponseEntity<ApiResponse<TournamentResponse>> cancelTournament(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Tournament cancelled",
                tournamentService.cancelTournament(id)));
    }

    @PatchMapping("/tournaments/{tournamentId}/participants/{userId}/disqualify")
    @Operation(summary = "Disqualify participant", description = "Disqualifies a user from a tournament.")
    public ResponseEntity<ApiResponse<TournamentParticipantResponse>> disqualifyParticipant(
            @PathVariable Long tournamentId,
            @PathVariable Long userId) {
        TournamentParticipantResponse participant = tournamentParticipantService
                .disqualifyParticipant(tournamentId, userId);
        return ResponseEntity.ok(ApiResponse.success("Participant disqualified", participant));
    }
}
