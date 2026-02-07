package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.event.service.EventWithRequest;
import ru.practicum.ewm.request.dto.RequestDTO;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {

    private final EventService eventService;
    private final EventWithRequest eventWithRequest;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public EventFullDto createEvent(@PathVariable @Positive @NotNull Long userId,
                                    @RequestBody @Valid NewEventDto newEventDto) {
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> findEventsByUserId(@PathVariable @Positive @NotNull Long userId,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                  @Positive @RequestParam(defaultValue = "10") int size) {
        return eventService.findEventByUserId(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findEventById(@PathVariable @Positive @NotNull Long userId,
                                      @PathVariable @Positive @NotNull Long eventId) {
        return eventService.findEventByIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable @Positive @NotNull Long userId,
                                    @PathVariable @Positive @NotNull Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        return eventService.updateEvent(userId, eventId, updateEventUserRequest);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestUser(@PathVariable @Positive @NotNull Long userId,
                                                            @PathVariable @Positive @NotNull Long eventId,
                                                            @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        return eventWithRequest.updateRequestUser(userId, eventId, request);
    }

    @GetMapping("/{eventId}/requests")
    public List<RequestDTO> getEventRequest(@PathVariable @Positive @NotNull Long userId,
                                            @PathVariable @Positive @NotNull Long eventId) {
        return eventWithRequest.getEventRequest(userId, eventId);
    }
}
