package ru.practicum.request.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.request.RequestDTO;
import ru.practicum.request.model.Request;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@Validated
public class RequestController {

    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDTO addRequestCurrentUser(@Positive @PathVariable Long userId,
                                            @RequestParam @NotNull @Positive Long eventId) {
        return requestService.addRequestCurrentUser(userId, eventId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDTO> getRequestsCurrentUser(@Positive @PathVariable Long userId) {
        return requestService.getRequestsCurrentUser(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public RequestDTO cancelRequestCurrentUser(@Positive @PathVariable Long userId, @Positive @PathVariable Long requestId) {
        return requestService.cancelRequestCurrentUser(userId, requestId);
    }

    //Добавил методы для feignClient
    @GetMapping("/findRequests")
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDTO> findRequestsByIds(@Positive @NotNull @PathVariable Long userId,
                                              @NotEmpty @RequestParam List<@NotNull @Positive Long> requestIds) {
        return requestService.findRequestsByIds(requestIds);
    }

    @PatchMapping("/updateRequests")
    public void updateRequestList(@Positive @NotNull @PathVariable Long userId,
                                  @NotEmpty @RequestBody List<RequestDTO> requestList) {
        requestService.saveRequestList(userId, requestList);
    }

    @GetMapping("/{eventId}")
    public List<RequestDTO> findRequestByEventId(@Positive @NotNull @PathVariable("eventId") Long userId,
                                                 @Positive @NotNull @PathVariable("eventId") Long eventId) {
        return requestService.getRequestByEventId(eventId);
    }
}