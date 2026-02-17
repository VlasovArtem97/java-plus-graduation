package ru.practicum.interaction.feignclient;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.config.Config;
import ru.practicum.interaction.dto.request.RequestDTO;

import java.util.List;

@FeignClient(name = "request-service", path = "/feign/requests", configuration = Config.class)
@Validated
public interface RequestFeignClient {

    @GetMapping("/findRequests")
    List<RequestDTO> findRequestsByIds(@NotEmpty @RequestParam("requestIds") List<@NotNull @Positive Long> requestIds);

    @PatchMapping("/updateRequests/{userId}")
    void updateRequestList(@Positive @NotNull @PathVariable("userId") Long userId,
                           @NotEmpty @RequestBody List<@NotNull RequestDTO> requestList);

    @GetMapping("/{eventId}")
    List<RequestDTO> findRequestByEventId(@Positive @NotNull @PathVariable("eventId") Long eventId);
}
