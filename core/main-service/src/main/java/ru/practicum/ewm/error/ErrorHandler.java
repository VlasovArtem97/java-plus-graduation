package ru.practicum.ewm.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.interaction.error.ApiError;
import ru.practicum.interaction.error.ConflictException;
import ru.practicum.interaction.error.GlobalErrorHandler;
import statsdto.exception.StatsServerUnavailable;

@Slf4j
@RestControllerAdvice
public class ErrorHandler extends GlobalErrorHandler {

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle409(Exception e) {
        log.error("Integrity constraint has been violated (Exception error): {}", e.getMessage());
        return build(HttpStatus.CONFLICT, "Integrity constraint has been violated.", e.getMessage());
    }

    //добавил для обработки, если не удасться подключиться к Stat-service
    @ExceptionHandler(StatsServerUnavailable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleStatsServerUnavailable(final StatsServerUnavailable e) {
        log.error("Не удалось подключиться к stat-service : {}", e.getMessage());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось подключиться к stat-service", e.getMessage());
    }
}