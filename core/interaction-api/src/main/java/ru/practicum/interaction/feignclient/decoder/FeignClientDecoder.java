package ru.practicum.interaction.feignclient.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.interaction.error.ApiError;
import ru.practicum.interaction.error.BadRequestException;
import ru.practicum.interaction.error.ConflictException;
import ru.practicum.interaction.error.NotFoundException;
import ru.practicum.interaction.error.feignexception.InternalServerErrorException;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class FeignClientDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String s, Response response) {
        //Проверяем, что у нас есть тело, чтобы не упало с ошибкой NullPointException
        if (response.body() != null) {
            try (InputStream is = response.body().asInputStream()) {
                //преобразовываем в ApiError через маппер
                ApiError apiError = objectMapper.readValue(is, ApiError.class);
                //Если распарсить не получится
                if (apiError == null || apiError.getMessage() == null) {
                    return defaultDecoder.decode(s, response);
                }
                String message = apiError.getMessage();
                return switch (response.status()) {
                    case 400 -> new BadRequestException(message);
                    case 404 -> new NotFoundException(message);
                    case 409 -> new ConflictException(message);
                    case 500 -> new InternalServerErrorException(message);
                    default -> defaultDecoder.decode(s, response);

                };
            } catch (IOException e) {
                log.error("Ошибка в декодере: {}", e.getMessage());
                return defaultDecoder.decode(s, response);
            }
        }
        return defaultDecoder.decode(s, response);
    }
}
