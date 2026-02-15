package ru.practicum.main.event.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.interaction.dto.event.*;
import ru.practicum.main.event.model.Event;

import java.util.List;

public interface EventService {

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> findEventByUserId(Long userId, int from, int size);

    EventFullDto findEventByIdAndEventId(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    Event findEventById(Long eventId);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto);

    List<EventFullDto> findEventByParamsAdmin(EventAdminParamDto eventParamDto);

    List<EventShortDto> findEventByParamsPublic(EventPublicParamsDto eventPublicParamsDto, HttpServletRequest request);

    EventFullDto findPublicEventById(Long eventId, HttpServletRequest request);

    Event findEventWithOutDto(Long userId, Long eventId);

    void saveEventWithRequest(Event event);

    List<Event> findEventsByids(List<Long> eventsIds);

    EventFullDto findEventByIdForFeign(Long eventId);

}
