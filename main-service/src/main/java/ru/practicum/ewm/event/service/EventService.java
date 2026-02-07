package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.Event;

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

}
