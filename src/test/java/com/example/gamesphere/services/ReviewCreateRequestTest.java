package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.ReviewCreateRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewCreateRequestTest {

    @Test
    void halfStarRatingIsAccepted() {
        ReviewCreateRequest request = new ReviewCreateRequest(1L, 4.5, "Very good");

        assertThat(request.isRatingStepValid()).isTrue();
    }

    @Test
    void arbitraryDecimalRatingIsRejected() {
        ReviewCreateRequest request = new ReviewCreateRequest(1L, 4.3, "Too precise");

        assertThat(request.isRatingStepValid()).isFalse();
    }
}
