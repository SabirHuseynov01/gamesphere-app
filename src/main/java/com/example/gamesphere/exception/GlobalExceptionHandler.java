package com.example.gamesphere.exception;

import com.example.gamesphere.dto.response.ApiResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Validation failed", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingOrInvalidRequestBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Request body is missing or invalid JSON", null));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingRequestParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Required request parameter is missing: " + ex.getParameterName(), null));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<?>> handleMultipartException(MultipartException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Multipart request is required. Use form-data with file field.", null));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingServletRequestPart(MissingServletRequestPartException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Required multipart field is missing: " + ex.getRequestPartName(), null));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Endpoint not found: " + ex.getResourcePath(), null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidOrder(InvalidOrderException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse<?>> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<ApiResponse<?>> handleProductOutOfStock(ProductOutOfStockException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication failed", null));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<?>> handleJwtException(JwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid or expired token", null));
    }

    @ExceptionHandler({
            ObjectOptimisticLockingFailureException.class,
            PessimisticLockingFailureException.class,
            CannotAcquireLockException.class
    })
    public ResponseEntity<ApiResponse<?>> handleConcurrentUpdate(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("The resource was changed by another request. Please retry.", null));
    }

    /**
     * What a unique or check constraint means to the caller.
     *
     * The database is the last line of defence: services check first, but two
     * concurrent requests can both pass that check and only one insert wins.
     * Without this, the loser leaves through the catch-all below as a 500 —
     * a server fault for what is really a conflicting request.
     */
    private static final Map<String, String> CONSTRAINT_MESSAGES = new LinkedHashMap<>();

    static {
        CONSTRAINT_MESSAGES.put("uk_products_game_platform_store_edition",
                "An offer for this edition already exists for that game, platform and store.");
        CONSTRAINT_MESSAGES.put("uk_users_email", "That email address is already registered.");
        CONSTRAINT_MESSAGES.put("uk_users_username", "That username is already taken.");
        CONSTRAINT_MESSAGES.put("uk_products_slug", "A product with this name already exists.");
        CONSTRAINT_MESSAGES.put("uk_games_slug", "A game with this title already exists.");
        CONSTRAINT_MESSAGES.put("uk_categories_name", "That category already exists.");
        CONSTRAINT_MESSAGES.put("uk_cart_items_cart_product", "That product is already in the cart.");
        CONSTRAINT_MESSAGES.put("uk_reviews_product_user", "You have already reviewed this product.");
        CONSTRAINT_MESSAGES.put("uk_reviews_game_user", "You have already reviewed this game.");
        CONSTRAINT_MESSAGES.put("uk_tournament_participants_user", "You have already joined this tournament.");
        CONSTRAINT_MESSAGES.put("uk_seller_profiles_shop_name", "That shop name is already taken.");
        CONSTRAINT_MESSAGES.put("uk_digital_codes_value", "That digital code already exists.");
        CONSTRAINT_MESSAGES.put("uk_payments_order", "A payment already exists for this order.");
        CONSTRAINT_MESSAGES.put("uk_orders_order_number", "That order number is already in use.");
        CONSTRAINT_MESSAGES.put("ck_products_discount_not_above_price",
                "The discount price cannot be higher than the price.");
        CONSTRAINT_MESSAGES.put("ck_reviews_rating_range", "The rating is outside the allowed range.");
        CONSTRAINT_MESSAGES.put("ck_tournaments_dates", "The end date must be after the start date.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // The driver message names the constraint; it is read only to pick a
        // message of ours, never returned — it carries SQL and column names.
        String cause = ex.getMostSpecificCause().getMessage();
        String detail = cause == null ? "" : cause.toLowerCase(Locale.ROOT);

        String message = CONSTRAINT_MESSAGES.entrySet().stream()
                .filter(entry -> detail.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("That change conflicts with data that already exists.");

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(message, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Internal server error: " + ex.getMessage(), null));

    }
}
