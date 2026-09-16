package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.AddCartItemRequest;
import com.example.gamesphere.dto.request.UpdateCartItemPlayerRequest;
import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.CartResponse;
import com.example.gamesphere.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Current user's shopping cart operations")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        CartResponse cart = cartService.getCurrentUserCart();
        return ResponseEntity.ok(
                ApiResponse.success("Cart retrieved", cart)
        );
    }

    @PostMapping("/items")
    @Operation(summary = "Add product to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddCartItemRequest request) {

        CartResponse cart = cartService.addToCart(request);

        return ResponseEntity.ok(
                ApiResponse.success("Product added to cart", cart)
        );
    }

    @PatchMapping("/items/{productId}/player-account")
    @Operation(summary = "Set the player account id of a cart item",
            description = "Fills in the in-game id a top-up needs, without changing its quantity.")
    public ResponseEntity<ApiResponse<CartResponse>> updatePlayerAccount(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemPlayerRequest request) {

        CartResponse cart = cartService.updatePlayerAccountId(productId, request.getPlayerAccountId());

        return ResponseEntity.ok(
                ApiResponse.success("Player account updated", cart)
        );
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove product from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @PathVariable Long productId) {

        CartResponse cart = cartService.removeFromCart(productId);

        return ResponseEntity.ok(
                ApiResponse.success("Product removed from cart", cart)
        );
    }

    @DeleteMapping("/items")
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCart();

        return ResponseEntity.ok(
                ApiResponse.success("Cart cleared successfully", null)
        );
    }
}
