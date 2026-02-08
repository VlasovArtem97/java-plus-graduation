package ru.practicum.ewm.event.validatelocaldatetime.checktimedifference;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.ewm.event.validatelocaldatetime.DateTimeValidate;

import java.time.LocalDateTime;

public class DateTimeValidateImpl implements ConstraintValidator<DateTimeValidate, LocalDateTime> {

    @Override
    public boolean isValid(LocalDateTime localDateTime, ConstraintValidatorContext constraintValidatorContext) {

        return localDateTime != null && !localDateTime.isBefore(LocalDateTime.now().plusHours(2));
    }
}
