package ru.practicum.request.service;

import ru.practicum.interaction.dto.request.RequestDTO;

import java.util.List;

public interface RequestService {

    RequestDTO addRequestCurrentUser(Long userId, Long eventId);

    List<RequestDTO> getRequestsCurrentUser(Long userId);

    RequestDTO cancelRequestCurrentUser(Long userId, Long requestId);

    List<RequestDTO> findRequestsByIds(List<Long> requestIds);

    void saveRequestList(Long userId, List<RequestDTO> requestList);

    List<RequestDTO> getRequestByEventId(Long eventId);
}