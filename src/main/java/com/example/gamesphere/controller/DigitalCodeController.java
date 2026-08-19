package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.DigitalCodeBatchRequest;
import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.DigitalCodeStockResponse;
import com.example.gamesphere.service.DigitalCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Digital Codes", description = "Seller-owned digital code inventory")
@SecurityRequirement(name = "bearerAuth")
public class DigitalCodeController {

    private final DigitalCodeService digitalCodeService;

    @PostMapping("/{productId}/codes")
    @Operation(summary = "Add digital codes", description = "Adds unique license codes to a seller-owned DIGITAL_CODE product.")
    public ResponseEntity<ApiResponse<DigitalCodeStockResponse>> addCodes(
            @PathVariable Long productId,
            @Valid @RequestBody DigitalCodeBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Digital codes added", digitalCodeService.addCodes(productId, request)));
    }
}
