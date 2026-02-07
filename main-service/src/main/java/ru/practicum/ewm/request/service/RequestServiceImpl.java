package ru.practicum.ewm.request.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.event.status.StateEvent;
import ru.practicum.ewm.request.dto.RequestDTO;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EventService eventService;
    private final RequestMapper requestMapper;

    @Autowired
    public RequestServiceImpl(RequestRepository requestRepository, UserService userService, EventService eventService, RequestMapper requestMapper) {
        this.requestRepository = requestRepository;
        this.userService = userService;
        this.eventService = eventService;
        this.requestMapper = requestMapper;
    }

    @Transactional
    @Override
    public RequestDTO addRequestCurrentUser(Long userId, Long eventId) {
        User user = userService.findUserById(userId);
        Event event = eventService.findEventById(eventId);

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId))
            throw new ConflictException("Данный запрос существует.");

        if (user.getId().equals(event.getInitiator().getId()))
            throw new ConflictException("Невозможно создать запрос на участие в своем же событии.");

        if (!event.getState().equals(StateEvent.PUBLISHED))
            throw new ConflictException("Нельзя участвовать в неопубликованном событии.");

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            throw new ConflictException("У события достигнут лимит запросов на участие.");
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setRequester(user);
        request.setEvent(event);

        if (!event.getRequestModeration()) {
            request.setRequestStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            if (event.getParticipantLimit() == 0) {
                request.setRequestStatus(RequestStatus.CONFIRMED);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            } else {
                request.setRequestStatus(RequestStatus.PENDING);
            }
        }
        return requestMapper.toRequestDTO(requestRepository.save(request));
    }

    @Override
    public List<RequestDTO> getRequestsCurrentUser(Long userId) {
        userService.findUserById(userId);

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toRequestDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public RequestDTO cancelRequestCurrentUser(Long userId, Long requestId) {
        userService.findUserById(userId);
        getRequestById(requestId);

        Request requestFromDatabase = requestRepository.findByIdAndRequesterId(requestId, userId);
        requestFromDatabase.setRequestStatus(RequestStatus.CANCELED);

        Request savedRequest = requestRepository.save(requestFromDatabase);

        return requestMapper.toRequestDTO(savedRequest);
    }

    @Override
    public List<Request> findRequestsByIds(List<Long> requestIds) {
        return requestRepository.findRequestsByIds(requestIds).orElseThrow(() ->
                new NotFoundException("Запросы(Request) с переданными ids: " + requestIds + " не найдены:"));
    }

    @Override
    public void saveRequestList(List<Request> requestList) {
        requestRepository.saveAll(requestList);
    }

    @Override
    public List<RequestDTO> getRequestByEventId(Long eventId) {
        List<Request> requestList = requestRepository.getRequestByEventId(eventId);
        return requestList.stream().map(requestMapper::toRequestDTO).toList();
    }

    private Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException(String.format("Запрос с id - %d не найден.", requestId)));
    }
}