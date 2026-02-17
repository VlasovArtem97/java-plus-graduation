package ru.practicum.request.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.request.RequestDTO;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/feign/requests")
@Validated
@RequiredArgsConstructor
public class FeignRequestController {

    private final RequestService requestService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/findRequests")
    List<RequestDTO> findRequestsByIds(@NotEmpty @RequestParam("requestIds") List<@NotNull @Positive Long> requestIds) {
        return requestService.findRequestsByIds(requestIds);
    }

    @PatchMapping("/updateRequests/{userId}")
    public void updateRequestList(@Positive @NotNull @PathVariable("userId") Long userId,
                                  @NotEmpty @RequestBody List<@NotNull RequestDTO> requestList) {
        requestService.saveRequestList(userId, requestList);
    }

    @GetMapping("/{eventId}")
    List<RequestDTO> findRequestByEventId(@Positive @NotNull @PathVariable("eventId") Long eventId) {
        return requestService.getRequestByEventId(eventId);
    }
}
