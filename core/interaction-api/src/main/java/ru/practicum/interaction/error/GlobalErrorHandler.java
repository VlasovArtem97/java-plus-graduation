package ru.practicum.interaction.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
public class GlobalErrorHandler {
    protected ApiError build(HttpStatus status, String reason, String message) {
        return ApiError.builder()
                .message(message)
                .reason(reason)
                .status(status.name())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerMethodArgumentNotValid(final MethodArgumentNotValidException e) {
        String allError = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Field: %s. Error: %s. Value: %s",
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()))
                .collect(Collectors.joining("; "));
        log.error("Incorrectly made request (MethodArgumentNotValidException error) : {}", allError);
        return build(HttpStatus.BAD_REQUEST, "Incorrectly made request", allError);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolation(final ConstraintViolationException e) {
        String allError = e.getConstraintViolations().stream()
                .map(violation -> String.format("Field: %s. Error: %s",
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .collect(Collectors.joining("; "));
        log.error("Incorrectly made request (ConstraintViolationException error): {}", allError);
        return build(HttpStatus.CONFLICT, "Incorrectly made request", allError);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameter(final MissingServletRequestParameterException e) {
        log.error("Incorrectly made request (MissingServletRequestParameterException error): {}", e.getParameterName());
        return build(HttpStatus.BAD_REQUEST, "Incorrectly made request", e.getParameterName());
    }


    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalState(final IllegalStateException e) {
        log.error("Incorrectly made request (IllegalStateException error): {}", e.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Incorrectly made request", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handle404(NotFoundException e) {
        log.error("The required object was not found (NotFoundException error): {}", e.getMessage());
        return build(HttpStatus.NOT_FOUND, "The required object was not found.", e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception e) {
        log.error("Internal server error - {}", e.getMessage());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error.", e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        log.error("Ошибка: {}", e.getMessage());
        return build(HttpStatus.CONFLICT, "Ошибка.", e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConflictException(final BadRequestException e) {
        log.error("Ошибка. Некорректный запрос: {}", e.getMessage());
        return build(HttpStatus.CONFLICT, "Ошибка.Некорректный запрос", e.getMessage());
    }
}