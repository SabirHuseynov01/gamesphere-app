package com.example.gamesphere.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDiscountPriceValidator.class)
public @interface ValidDiscountPrice {

    String message() default "Discount price must be lower than price";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
