package ru.practicum.ewm.request.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.RequestDTO;
import ru.practicum.ewm.request.service.RequestService;

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
}