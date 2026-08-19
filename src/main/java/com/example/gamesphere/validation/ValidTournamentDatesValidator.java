package com.example.gamesphere.validation;

import com.example.gamesphere.dto.request.TournamentCreateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidTournamentDatesValidator implements ConstraintValidator<ValidTournamentDates, TournamentCreateRequest> {

    @Override
    public boolean isValid(TournamentCreateRequest value, ConstraintValidatorContext context) {
        if (value == null || value.getStartDate() == null || value.getEndDate() == null) {
            return true;
        }

        return value.getEndDate().isAfter(value.getStartDate());
    }
}
