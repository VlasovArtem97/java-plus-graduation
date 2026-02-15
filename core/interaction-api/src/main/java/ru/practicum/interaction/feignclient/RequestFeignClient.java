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

@FeignClient(name = "request-service", path = "/users", configuration = Config.class)
@Validated
public interface RequestFeignClient {

    @GetMapping("/{userId}/requests/findRequests")
    List<RequestDTO> findRequestsByIds(@Positive @NotNull @PathVariable("userId") Long userId,
                                       @NotEmpty @RequestParam("requestIds") List<@NotNull @Positive Long> requestIds);

    @PatchMapping("/{userId}/requests/updateRequests")
    void updateRequestList(@Positive @NotNull @PathVariable("userId") Long userId,
                           @NotEmpty @RequestBody List<RequestDTO> requestList);

    @GetMapping("/{userId}/requests/{eventId}")
    List<RequestDTO> findRequestByEventId(@Positive @NotNull @PathVariable("userId") Long userId,
                                          @Positive @NotNull @PathVariable("eventId") Long eventId);
}
