package ru.practicum.main.error;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    //добавил для grpc исключения
    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ApiError> handleStatusRuntimeException(final StatusRuntimeException e) {
        log.error("Ошибка при взаимодействии с сервисом статистики (grpc): {}", e.getStatus().getDescription());
        Status.Code code = e.getStatus().getCode();

        HttpStatus httpStatus = switch (code) {
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        ApiError apiError = ApiError.builder()
                .status(httpStatus.name())
                .reason("Ошибка при работе с Grpc")
                .message(e.getStatus().getDescription())
                .build();
        return new ResponseEntity<>(apiError, httpStatus);
    }
}