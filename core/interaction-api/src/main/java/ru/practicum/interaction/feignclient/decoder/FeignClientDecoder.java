package ru.practicum.interaction.feignclient.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import ru.practicum.interaction.error.ApiError;
import ru.practicum.interaction.error.BadRequestException;
import ru.practicum.interaction.error.ConflictException;
import ru.practicum.interaction.error.NotFoundException;
import ru.practicum.interaction.error.feignexception.InternalServerErrorException;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class FeignClientDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;//регистрируем для работы с LocalDateTime
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String s, Response response) {
        //Проверяем, что у нас есть тело, чтобы не упало с ошибкой NullPointException
        if (response.body() != null) {
            try (InputStream is = response.body().asInputStream()) {
                //преобразовываем в ApiError через маппер
                ApiError apiError = objectMapper.readValue(is, ApiError.class);
                String message = apiError.getMessage();
                return switch (response.status()) {
                    case 400 -> new BadRequestException(message);
                    case 404 -> new NotFoundException(message);
                    case 409 -> new ConflictException(message);
                    case 500 -> new InternalServerErrorException(message);
                    default -> defaultDecoder.decode(s, response);

                };
            } catch (IOException e) {
                return new RuntimeException("Ошибка в декодере: " + e.getMessage());
            }
        }
        return defaultDecoder.decode(s, response);
    }
}
