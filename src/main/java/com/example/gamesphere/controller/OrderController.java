package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.CreateOrderRequest;
import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.OrderResponse;
import com.example.gamesphere.dto.response.PageResponse;
import com.example.gamesphere.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order creation and current user order queries")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order", description = "Creates an order from selected product ids for the current user.")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    @PostMapping("/from-cart")
    @Operation(
            summary = "Create order from cart",
            description = "Creates a pending order from the current cart and clears the cart.")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrderFromCart() {
        OrderResponse order = orderService.createOrderFromCart();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created from cart", order));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by id", description = "Returns order details by id.")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved", order));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by number", description = "Returns order details by unique order number.")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderResponse order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved", order));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my orders", description = "Returns paginated orders for the current authenticated user.")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(Pageable pageable) {
        PageResponse<OrderResponse> orders = orderService.getMyOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success("My orders retrieved", orders));
    }
}
