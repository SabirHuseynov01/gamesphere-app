package com.example.gamesphere.validation;

import com.example.gamesphere.dto.request.ProductCreateRequest;
import com.example.gamesphere.dto.request.ProductUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class ValidDiscountPriceValidator implements ConstraintValidator<ValidDiscountPrice, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BigDecimal price = null;
        BigDecimal discountPrice = null;

        if (value instanceof ProductCreateRequest request) {
            price = request.getPrice();
            discountPrice = request.getDiscountPrice();
        } else if (value instanceof ProductUpdateRequest request) {
            price = request.getPrice();
            discountPrice = request.getDiscountPrice();
        }

        if (price == null || discountPrice == null) {
            return true;
        }

        return discountPrice.compareTo(BigDecimal.ZERO) > 0 && discountPrice.compareTo(price) < 0;
    }
}
