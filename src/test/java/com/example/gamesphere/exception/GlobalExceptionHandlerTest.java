package com.example.gamesphere.exception;

import com.example.gamesphere.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /** What PostgreSQL actually hands back when a unique index rejects a row. */
    private static DataIntegrityViolationException violation(String constraint) {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new SQLException("ERROR: duplicate key value violates unique constraint \"" + constraint + "\""));
    }

    @Test
    void aDuplicateEditionOfferIsAConflictWithAnExplanation() {
        ResponseEntity<ApiResponse<?>> response =
                handler.handleDataIntegrityViolation(violation("uk_products_game_platform_store_edition"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage())
                .isEqualTo("An offer for this edition already exists for that game, platform and store.");
    }

    @Test
    void aTakenEmailIsNamedRatherThanReportedAsAServerFault() {
        ResponseEntity<ApiResponse<?>> response = handler.handleDataIntegrityViolation(violation("uk_users_email"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("That email address is already registered.");
    }

    @Test
    void anUnmappedConstraintStillAnswersWithoutLeakingTheQuery() {
        ResponseEntity<ApiResponse<?>> response = handler.handleDataIntegrityViolation(
                violation("uk_something_we_have_not_mapped"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage())
                .isEqualTo("That change conflicts with data that already exists.");
        // The driver text names tables and columns; it must not reach the caller.
        assertThat(response.getBody().getMessage()).doesNotContain("duplicate key", "constraint");
    }

    @Test
    void aViolationWithNoCauseMessageDoesNotBlowUp() {
        ResponseEntity<ApiResponse<?>> response =
                handler.handleDataIntegrityViolation(new DataIntegrityViolationException(null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage())
                .isEqualTo("That change conflicts with data that already exists.");
    }
}
