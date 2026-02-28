package ru.practicum.main.event.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.RequestDTO;

import java.util.List;

public interface EventWithRequest {

    EventRequestStatusUpdateResult updateRequestUser(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<RequestDTO> getEventRequest(Long userId, Long eventId);

    void likeEvent(Long eventId, Long userId);
}
