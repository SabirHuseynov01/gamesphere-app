package com.example.gamesphere.util;

import com.example.gamesphere.exception.BusinessException;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DiscountCalculator {

    public BigDecimal finalPrice(BigDecimal price, BigDecimal discountPrice) {
        if (discountPrice == null) {
            return price;
        }
        validateDiscount(price, discountPrice);
        return discountPrice;
    }

    public BigDecimal discountPercentage(BigDecimal price, BigDecimal discountPrice) {
        if (discountPrice == null || price == null || BigDecimal.ZERO.compareTo(price) == 0) {
            return BigDecimal.ZERO;
        }
        validateDiscount(price, discountPrice);
        BigDecimal discountAmount = price.subtract(discountPrice);
        return discountAmount.multiply(BigDecimal.valueOf(100))
                .divide(price, 2, RoundingMode.HALF_UP);
    }

    public void validateDiscount(BigDecimal price, BigDecimal discountPrice) {
        if (price == null || discountPrice == null) {
            return;
        }
        if (discountPrice.compareTo(price) >= 0) {
            throw new BusinessException("Discount price must be lower than price.");
        }
        if (discountPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Discount price must be greater than zero.");
        }
    }
}
