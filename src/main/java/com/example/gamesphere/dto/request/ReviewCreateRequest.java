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

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("5.0")
    private Double rating;

    @NotBlank
    private String comment;

    @AssertTrue(message = "Rating must use 0.5 increments")
    public boolean isRatingStepValid() {
        if (rating == null) {
            return true;
        }
        double doubledRating = rating * 2;
        return Math.abs(doubledRating - Math.rint(doubledRating)) < 0.000001;
    }
}
