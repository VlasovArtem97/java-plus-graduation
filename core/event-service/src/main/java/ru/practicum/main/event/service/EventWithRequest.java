package ru.practicum.main.event.service;

import ru.practicum.interaction.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.RequestDTO;

import java.util.List;

public interface EventWithRequest {

    EventRequestStatusUpdateResult updateRequestUser(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<RequestDTO> getEventRequest(Long userId, Long eventId);
}
