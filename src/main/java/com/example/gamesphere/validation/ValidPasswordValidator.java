package com.example.gamesphere.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.length() >= 8
                && value.matches(".*[A-Z].*")
                && value.matches(".*[a-z].*")
                && value.matches(".*\\d.*");
    }


}
