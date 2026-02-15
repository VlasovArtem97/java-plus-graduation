package ru.practicum.interaction.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.interaction.feignclient.decoder.FeignClientDecoder;

import java.time.LocalDateTime;

@Configuration
public class Config {

    @Bean
    public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
        //Создал Маппер для defaultDecoder, поскольку есть исключения, которые могут прийти не прописанные в нашем приложении
        ObjectMapper objectMapperUpdate = objectMapper.copy();
        objectMapperUpdate.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapperUpdate.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        objectMapperUpdate.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, true);
        return new FeignClientDecoder(objectMapperUpdate);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configOverride(LocalDateTime.class)
                .setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd HH:mm:ss"));
        return mapper;
    }
}
