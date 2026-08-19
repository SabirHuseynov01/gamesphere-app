package com.example.gamesphere.controller;

import com.example.gamesphere.dto.request.ProductCreateRequest;
import com.example.gamesphere.dto.request.ProductFilterRequest;
import com.example.gamesphere.dto.request.ProductUpdateRequest;
import com.example.gamesphere.dto.response.ApiResponse;
import com.example.gamesphere.dto.response.PageResponse;
import com.example.gamesphere.dto.response.ProductImageResponse;
import com.example.gamesphere.dto.response.ProductResponse;
import com.example.gamesphere.service.ProductImageService;
import com.example.gamesphere.service.ProductQueryService;
import com.example.gamesphere.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog, seller product management, images and recommendations")
public class ProductController {

    private final ProductQueryService productQueryService;
    private final ProductService productService;
    private final ProductImageService productImageService;

    @PostMapping
    @Operation(summary = "Create product", description = "Creates a new marketplace product for the current seller.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", response));
    }

    @PostMapping("/{id}/images")
    @Operation(summary = "Upload product image",
            description = "Uploads a product image and optionally marks it as primary.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ProductImageResponse>> uploadProductImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean primary,
            @RequestParam(defaultValue = "0") int sortOrder) {
        ProductImageResponse image = productImageService.uploadProductImage(id, file, primary, sortOrder);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product image uploaded", image));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product owned by the current seller.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated", response));
    }

    @GetMapping
    @Operation(summary = "Filter products",
            description = "Returns paginated products by search, genre, platform, type, status and price filters.")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            ProductFilterRequest filter, Pageable pageable) {
        PageResponse<ProductResponse> page = productQueryService.getAllProducts(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Products list", page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id", description = "Returns product details by product id.")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        ProductResponse response = productQueryService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product found", response));
    }

    @GetMapping("/active")
    @Operation(summary = "List active products", description = "Returns all active products.")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getActiveProducts() {
        List<ProductResponse> products = productQueryService.getActiveProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "List seller products", description = "Returns products created by a selected seller.")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsBySeller(@PathVariable Long sellerId) {
        List<ProductResponse> products = productQueryService.getProductsBySeller(sellerId);
        return ResponseEntity.ok(ApiResponse.success("Seller products retrieved", products));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug", description = "Returns product details by SEO-friendly slug.")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySlug(@PathVariable String slug){
        ProductResponse response = productQueryService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Product found", response));
    }

    @GetMapping("/{id}/recommendations/same-genre")
    @Operation(summary = "Recommend same genre products",
            description = "Returns products with the same genre as the selected product.")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getSameGenreRecommendations(@PathVariable Long id) {
        List<ProductResponse> products = productQueryService.getSameGenreRecommendations(id);
        return ResponseEntity.ok(ApiResponse.success("Same genre recommendations retrieved", products));
    }

    @GetMapping("/recommendations/top-reviewed")
    @Operation(summary = "Recommend top reviewed products", description = "Returns products ordered by review count.")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getTopReviewedRecommendations() {
        List<ProductResponse> products = productQueryService.getTopReviewedRecommendations();
        return ResponseEntity.ok(ApiResponse.success("Top reviewed recommendations retrieved", products));
    }

    @GetMapping("/recommendations/wishlist/{userId}")
    @Operation(summary = "Recommend from wishlist categories",
            description = "Returns category-based recommendations using a user's wishlist.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getWishlistCategoryRecommendations(@PathVariable Long userId) {
        List<ProductResponse> products = productQueryService.getWishlistCategoryRecommendations(userId);
        return ResponseEntity.ok(ApiResponse.success("Wishlist based recommendations retrieved", products));
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "List product images", description = "Returns uploaded images for a product.")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getProductImages(@PathVariable Long id) {
        List<ProductImageResponse> images = productImageService.getProductImages(id);
        return ResponseEntity.ok(ApiResponse.success("Product images retrieved", images));
    }
}
