package ru.practicum.ewm.event.validatelocaldatetime;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.ewm.event.validatelocaldatetime.checktimedifference.DateTimeValidateImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Constraint(validatedBy = {DateTimeValidateImpl.class})
@Target({ElementType.FIELD}) // аннотация для класса
@Retention(RetentionPolicy.RUNTIME)
public @interface DateTimeValidate {
    String message() default "Дата и время не должны быть null и должны быть не раньше чем через 2 часа";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
