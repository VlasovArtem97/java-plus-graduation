package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.enums.StateEventDto;
import ru.practicum.interaction.dto.request.RequestDTO;
import ru.practicum.interaction.dto.request.enums.RequestStatusDto;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.error.ConflictException;
import ru.practicum.interaction.error.NotFoundException;
import ru.practicum.interaction.feignclient.EventFeignClient;
import ru.practicum.interaction.feignclient.UserFeignClient;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserFeignClient userFeignClient;
    private final EventFeignClient eventFeignClient;
    private final RequestMapper requestMapper;

    @Transactional
    @Override
    public RequestDTO addRequestCurrentUser(Long userId, Long eventId) {
        UserDto user = userFeignClient.findUserById(userId);
        EventFullDto event = eventFeignClient.findEventByIdForFeign(eventId);

        Long confirmedRequests = event.getConfirmedRequests();

        if (requestRepository.existsByRequesterIdInAndEventIdIn(List.of(userId), List.of(eventId)))
            throw new ConflictException("Данный запрос существует.");

        if (user.getId().equals(event.getInitiator()))
            throw new ConflictException("Невозможно создать запрос на участие в своем же событии.");

        if (!event.getState().equals(StateEventDto.PUBLISHED))
            throw new ConflictException("Нельзя участвовать в неопубликованном событии.");

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            throw new ConflictException("У события достигнут лимит запросов на участие.");
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setRequesterId(user.getId());
        request.setEventId(event.getId());

        if (!event.getRequestModeration()) {
            request.setRequestStatus(RequestStatusDto.CONFIRMED);
            confirmedRequests ++;
        } else {
            if (event.getParticipantLimit() == 0) {
                request.setRequestStatus(RequestStatusDto.CONFIRMED);
                confirmedRequests ++;
            } else {
                request.setRequestStatus(RequestStatusDto.PENDING);
            }
        }
        //Если есть одобренные заявки отправляем в event на сохранения новых данных
        if(!event.getConfirmedRequests().equals(confirmedRequests)) {
            event.setConfirmedRequests(confirmedRequests);
            eventFeignClient.updateConfirmedRequestsFromFeign(eventId, event);
        }

        return requestMapper.toRequestDTO(requestRepository.save(request));
    }

    @Override
    public List<RequestDTO> getRequestsCurrentUser(Long userId) {
        userFeignClient.findUserById(userId);

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toRequestDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public RequestDTO cancelRequestCurrentUser(Long userId, Long requestId) {
        userFeignClient.findUserById(userId);
        getRequestById(requestId);

        Request requestFromDatabase = requestRepository.findByIdAndRequesterId(requestId, userId);
        requestFromDatabase.setRequestStatus(RequestStatusDto.CANCELED);

        Request savedRequest = requestRepository.save(requestFromDatabase);

        return requestMapper.toRequestDTO(savedRequest);
    }

    @Override
    public List<RequestDTO> findRequestsByIds(List<Long> requestIds) {
        log.info("Получен запрос на получение Request's c id: {}", requestIds);
        List<Request> requests = requestRepository.findRequestsByIds(requestIds);
        if (requests.isEmpty()) {
            log.error("Отсутстуют Request c id: {}", requestIds);
            throw new NotFoundException("Запросы(Request) с переданными ids: " + requestIds + " не найдены:");
        }

        //Находим idRequest  из найденных Request
        Set<Long> requestId = requests.stream()
                .map(Request::getId)
                .collect(Collectors.toSet());

        //Смотрим, найдены ли все Request
        Set<Long> missingIds = requestIds.stream()
                .filter(id -> !requestId.contains(id))
                .collect(Collectors.toSet());

        if (!missingIds.isEmpty()) {
            log.error("Отсутстуют Request c id: {}", missingIds);
            throw new NotFoundException("Запросы(Request) с переданными ids: " + missingIds + " не найдены:");
        }

        List<RequestDTO> requestDTOList = requests.stream()
                .map(requestMapper::toRequestDTO)
                .toList();
        log.debug("Найдены Request: {}", requestDTOList);
        return requestDTOList;
    }

    @Transactional
    @Override
    public void saveRequestList(Long userId, List<RequestDTO> requestList) {
        log.info("Получен запрос на сохранения обновленных статусов Request's: {}", requestList);

        //Проверяем на всякий случай, что такие Request существуют
        Set<Long> eventsId = requestList.stream()
                .map(RequestDTO::getEventId)
                .collect(Collectors.toSet());

        Set<Long> requesterIds = requestList.stream()
                .map(RequestDTO::getRequesterId)
                .collect(Collectors.toSet());

        if (requestRepository.existsByRequesterIdInAndEventIdIn(requesterIds, eventsId)) {
            List<Request> requestDTOList = requestList.stream()
                    .map(requestMapper::toRequest)
                    .toList();
            List<Request> updateStatusRequest = requestRepository.saveAll(requestDTOList);
            log.debug("Обновленный объекты: {}", updateStatusRequest);
        } else {
            log.error("В переданном списке, есть Request c eventId которых не существуют. EventIds: {}", eventsId);
            throw new ConflictException("В переданном списке, есть Request c eventId которых не существуют. EventIds: " +
                    eventsId);
        }
    }

    @Override
    public List<RequestDTO> getRequestByEventId(Long eventId) {
        log.info("Получен запрос на получение Request по eventId: {}", eventId);
        List<Request> requestList = requestRepository.getRequestByEventId(eventId);
        if (requestList.isEmpty()) {
            log.error("Request c eventId: [ {} ] не найден", eventId);
            throw new NotFoundException("Request c eventId: [ " + eventId + " ] не найден");
        }
        return requestList.stream()
                .map(requestMapper::toRequestDTO)
                .toList();
    }

    private Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException(String.format("Запрос с id - %d не найден.", requestId)));
    }
}