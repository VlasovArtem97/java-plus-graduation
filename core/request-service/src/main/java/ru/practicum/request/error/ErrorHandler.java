package ru.practicum.request.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.interaction.error.ApiError;
import ru.practicum.interaction.error.GlobalErrorHandler;

@Slf4j
@RestControllerAdvice
public class ErrorHandler extends GlobalErrorHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle409(final DataIntegrityViolationException e) {
        log.error("Integrity constraint has been violated (Exception error): {}", e.getMessage());
        return build(HttpStatus.CONFLICT, "Integrity constraint has been violated.",
                e.getMostSpecificCause().getMessage());
    }
}