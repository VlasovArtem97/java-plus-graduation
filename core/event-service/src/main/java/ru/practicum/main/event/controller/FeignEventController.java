package ru.practicum.main.event.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.main.event.service.EventService;

@RestController
@RequestMapping(path = "/feign/events")
@RequiredArgsConstructor
@Validated
public class FeignEventController {

    private final EventService eventService;

    @GetMapping("/findEvent/{eventId}")
    public EventFullDto findEventByIdForFeign(@PathVariable @Positive @NotNull Long eventId) {
        return eventService.findEventByIdForFeign(eventId);
    }

    @PatchMapping("/updateConfirmedRequests/{eventId}")
    public void updateConfirmedRequestsFromFeign(@Positive @NotNull @PathVariable Long eventId,
                                                 @NotNull @RequestBody EventFullDto eventFullDto) {
        eventService.updateConfirmedRequestsFromFeign(eventId, eventFullDto);
    }
}
