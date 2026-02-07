package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.RequestDTO;
import ru.practicum.ewm.request.model.Request;

import java.util.List;

public interface RequestService {

    RequestDTO addRequestCurrentUser(Long userId, Long eventId);

    List<RequestDTO> getRequestsCurrentUser(Long userId);

    RequestDTO cancelRequestCurrentUser(Long userId, Long requestId);

    List<Request> findRequestsByIds(List<Long> requestIds);

    void saveRequestList(List<Request> requestList);

    List<RequestDTO> getRequestByEventId(Long eventId);
}