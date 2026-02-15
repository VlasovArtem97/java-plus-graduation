package ru.practicum.interaction.feignclient;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.interaction.config.Config;
import ru.practicum.interaction.dto.event.EventFullDto;

@FeignClient(name = "event-service", path = "/events", configuration = Config.class)
public interface EventFeignClient {

    @GetMapping("/findEvent/{eventId}")
    EventFullDto findEventByIdForFeign(@PathVariable @Positive @NotNull Long eventId);
}
