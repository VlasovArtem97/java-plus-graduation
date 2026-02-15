package ru.practicum.main.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.event.EventAdminParamDto;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.UpdateEventAdminRequestDto;
import ru.practicum.main.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {

    private final EventService eventService;

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdmin(@PathVariable @Positive @NotNull Long eventId,
                                         @Valid @RequestBody UpdateEventAdminRequestDto updateEventAdminRequestDto) {
        return eventService.updateEventAdmin(eventId, updateEventAdminRequestDto);
    }

    @GetMapping
    public List<EventFullDto> findEventByParamsAdmin(@RequestParam(required = false) List<Long> users,
                                                     @RequestParam(required = false) List<String> state,
                                                     @RequestParam(required = false) List<Long> categories,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                     @RequestParam(defaultValue = "0") int from,
                                                     @RequestParam(defaultValue = "10") int size) {
        return eventService.findEventByParamsAdmin(EventAdminParamDto.builder()
                .users(users)
                .state(state)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build());
    }

}
