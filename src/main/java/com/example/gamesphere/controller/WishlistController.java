package com.example.gamesphere.controller;

import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.WishlistResponse;
import com.example.gamesphere.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Current user's wishlist operations")
@SecurityRequirement(name = "bearerAuth")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get wishlist", description = "Returns current authenticated user's wishlist.")
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist() {
        WishlistResponse wishlist = wishlistService.getCurrentUserWishlist();
        return ResponseEntity.ok(ApiResponse.success("Wishlist retrieved", wishlist));
    }

    @PostMapping("/add/{productId}")
    @Operation(summary = "Add product to wishlist", description = "Adds a product to current user's wishlist.")
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(@PathVariable Long productId) {
        WishlistResponse wishlist = wishlistService.addToWishlist(productId);
        return ResponseEntity.ok(ApiResponse.success("Product added to wishlist", wishlist));
    }

    @DeleteMapping("/remove/{productId}")
    @Operation(summary = "Remove product from wishlist", description = "Removes a product from current user's wishlist.")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeFromWishlist(@PathVariable Long productId) {
        WishlistResponse wishlist = wishlistService.removeFromWishlist(productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist", wishlist));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear wishlist", description = "Removes all products from current user's wishlist.")
    public ResponseEntity<ApiResponse<Void>> clearWishlist() {
        wishlistService.clearWishlist();
        return ResponseEntity.ok(ApiResponse.success("Wishlist cleared successfully", null));
    }
}
