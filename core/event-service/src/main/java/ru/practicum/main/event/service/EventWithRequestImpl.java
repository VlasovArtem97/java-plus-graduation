package ru.practicum.main.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.RequestDTO;
import ru.practicum.interaction.dto.request.enums.RequestStatusDto;
import ru.practicum.interaction.error.ConflictException;
import ru.practicum.interaction.feignclient.RequestFeignClient;
import ru.practicum.interaction.feignclient.UserFeignClient;
import ru.practicum.main.event.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class EventWithRequestImpl implements EventWithRequest {

    private final EventService eventService;
    private final UserFeignClient userFeignClient;
    private final RequestFeignClient requestFeignClient;

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequestUser(Long userId, Long eventId,
                                                            EventRequestStatusUpdateRequest request) {
        userFeignClient.findUserById(userId);
        Event event = eventService.findEventWithOutDto(userId, eventId);
        List<RequestDTO> requestList = requestFeignClient.findRequestsByIds(request.getRequestIds());
        List<RequestDTO> confirmedRequests = new ArrayList<>();
        List<RequestDTO> rejectedRequests = new ArrayList<>();

        if (Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit()) &&
                request.getStatus().equals(RequestStatusDto.CONFIRMED)) {
            throw new ConflictException("У события достигнут лимит запросов на участие.");
        }

        for (RequestDTO req : requestList) {
            if (!req.getRequestStatus().equals(RequestStatusDto.PENDING)) {
                throw new ConflictException("Заявку c Id: " + req.getId() +
                        " можно одобрить, если у нее статус: " + RequestStatusDto.PENDING);
            }
        }

        if ((!event.getRequestModeration() || event.getParticipantLimit().equals(0L)) &&
                request.getStatus().equals(RequestStatusDto.CONFIRMED)) {
            for (RequestDTO req : requestList) {
                req.setRequestStatus(RequestStatusDto.CONFIRMED);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                confirmedRequests.add(req);
            }
        } else if ((!event.getRequestModeration() || event.getParticipantLimit().equals(0L)) &&
                request.getStatus().equals(RequestStatusDto.REJECTED)) {
            for (RequestDTO req : requestList) {
                req.setRequestStatus(RequestStatusDto.REJECTED);
                rejectedRequests.add(req);
            }
        } else if (request.getStatus().equals(RequestStatusDto.REJECTED)) {
            for (RequestDTO req : requestList) {
                req.setRequestStatus(RequestStatusDto.REJECTED);
                rejectedRequests.add(req);
            }
        } else {
            for (RequestDTO req : requestList) {
                if (Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
                    req.setRequestStatus(RequestStatusDto.REJECTED);
                    rejectedRequests.add(req);
                } else {
                    req.setRequestStatus(RequestStatusDto.CONFIRMED);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                    confirmedRequests.add(req);
                }
            }
        }

        List<RequestDTO> allRequest = new ArrayList<>();
        allRequest.addAll(confirmedRequests);
        allRequest.addAll(rejectedRequests);

        requestFeignClient.updateRequestList(userId, allRequest);
        eventService.saveEventWithRequest(event);


        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    @Override
    public List<RequestDTO> getEventRequest(Long userId, Long eventId) {
        userFeignClient.findUserById(userId);
        eventService.findEventWithOutDto(userId, eventId);
        return requestFeignClient.findRequestByEventId(eventId);
    }
}
