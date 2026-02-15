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
        return new FeignClientDecoder(objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // ОТКЛЮЧАЕМ запись дат в виде чисел (timestamps)
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // ОТКЛЮЧАЕМ ожидание чисел при чтении
        mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        mapper.configOverride(LocalDateTime.class)
                .setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd HH:mm:ss"));
        return mapper;
    }
}
