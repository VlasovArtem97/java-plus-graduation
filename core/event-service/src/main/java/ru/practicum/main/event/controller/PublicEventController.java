package ru.practicum.main.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventPublicParamsDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.enums.SortForParamPublicEventDto;
import ru.practicum.main.event.service.EventService;
import ru.practicum.main.event.service.EventWithRequest;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Validated
public class PublicEventController {

    private final EventService eventService;
    private final EventWithRequest eventWithRequest;

    public static final String HEADER_USER_ID = "X-EWM-USER-ID";

    @GetMapping
    public List<EventShortDto> findEventByParamsPublic(@RequestParam(required = false) String text,
                                                       @RequestParam(required = false) List<Long> categories,
                                                       @RequestParam(required = false) Boolean paid,
                                                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                       @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                       @RequestParam(required = false) String sort,
                                                       @RequestParam(defaultValue = "0") int from,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       HttpServletRequest request) {
        SortForParamPublicEventDto sortParam = SortForParamPublicEventDto.from(sort).orElse(null);
        return eventService.findEventByParamsPublic(EventPublicParamsDto.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sortParam)
                .from(from)
                .size(size)
                .build(), request);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findPublicEventById(@Positive @NotNull @RequestHeader(HEADER_USER_ID) Long userId,
                                            @PathVariable @Positive @NotNull Long eventId,
                                            HttpServletRequest request) {
        return eventService.findPublicEventById(userId, eventId, request);
    }

    @GetMapping("/recommendations")
    List<EventShortDto> getRecommendations(@Positive @NotNull @RequestHeader(HEADER_USER_ID) Long userId,
                                           @Positive @RequestParam(defaultValue = "10") int size) {
        return eventService.getRecommendations(userId, size);
    }

    @PutMapping("/{eventId}/like")
    public void likeEvent(
            @Positive @NotNull @PathVariable Long eventId,
            @Positive @NotNull @RequestHeader(HEADER_USER_ID) Long userId) {
        eventWithRequest.likeEvent(eventId, userId);
    }
}
