package com.example.gamesphere.controller;

import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.LibraryItemResponse;
import com.example.gamesphere.service.EntitlementService;
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
@RequestMapping("/api/library")
@RequiredArgsConstructor
@Tag(name = "Library", description = "Current user's delivered digital products")
@SecurityRequirement(name = "bearerAuth")
public class LibraryController {

    private final EntitlementService entitlementService;

    @GetMapping
    @Operation(summary = "Get my library")
    public ResponseEntity<ApiResponse<List<LibraryItemResponse>>> getMyLibrary() {
        return ResponseEntity.ok(ApiResponse.success(
                "Library retrieved", entitlementService.getMyLibrary()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get my library item")
    public ResponseEntity<ApiResponse<LibraryItemResponse>> getMyLibraryItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Library item retrieved", entitlementService.getMyLibraryItem(id)));
    }
}
