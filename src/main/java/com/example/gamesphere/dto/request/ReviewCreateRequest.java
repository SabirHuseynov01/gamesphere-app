package com.example.gamesphere.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {

    private Long productId;

    private Long gameId;

    @AssertTrue(message = "Either gameId or productId must be provided")
    public boolean isTargetProvided() {
        return gameId != null || productId != null;
    }

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("5.0")
    private Double rating;

    @NotBlank
    private String comment;

    public ReviewCreateRequest(Long productId, Double rating, String comment) {
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
    }

    @AssertTrue(message = "Rating must use 0.5 increments")
    public boolean isRatingStepValid() {
        if (rating == null) {
            return true;
        }
        double doubledRating = rating * 2;
        return Math.abs(doubledRating - Math.rint(doubledRating)) < 0.000001;
    }
}
