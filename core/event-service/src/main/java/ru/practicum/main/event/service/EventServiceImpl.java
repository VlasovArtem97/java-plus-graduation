package ru.practicum.main.event.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.proto.messages.ActionTypeProto;
import ru.practicum.ewm.stats.proto.messages.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.messages.UserActionProto;
import ru.practicum.interaction.dto.event.*;
import ru.practicum.interaction.dto.event.enums.SortForParamPublicEventDto;
import ru.practicum.interaction.dto.event.enums.StateEventDto;
import ru.practicum.interaction.dto.event.enums.StateForUpdateEventDto;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.error.ConflictException;
import ru.practicum.interaction.error.NotFoundException;
import ru.practicum.interaction.feignclient.UserFeignClient;
import ru.practicum.interaction.utill.PageRequestUtil;
import ru.practicum.main.category.mapper.CategoryMapper;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.service.CategoryService;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.mapper.LocationMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.Location;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.event.repository.LocationRepository;
import ru.practicum.statsclient.AnalyzerClient;
import ru.practicum.statsclient.CollectorClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final UserFeignClient userFeignClient;
    private final EventMapper eventMapper;
    private final CategoryMapper categoryMapper;
    private final LocationMapper locationMapper;
    private final CategoryService categoryService;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CollectorClient collectorClient;
    private final AnalyzerClient analyzerClient;


    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        UserDto user = userFeignClient.findUserById(userId);
        Category category = categoryMapper.toCategory(categoryService.getCategory(newEventDto.getCategory()));
        Location location = locationRepository.save(locationMapper.toLocation(newEventDto.getLocation()));
        Event event = eventRepository.save(eventMapper.toEvent(newEventDto, user, category, location));
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> findEventByUserId(Long userId, int from, int size) {
        userFeignClient.findUserById(userId);
        Pageable pageable = PageRequestUtil.of(from, size, Sort.by("id").ascending());
        return eventRepository.findEventByUserId(userId, pageable).getContent().stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto findEventByIdAndEventId(Long userId, Long eventId) {
        userFeignClient.findUserById(userId);
        Event event = findEventWithOutDto(userId, eventId);
        return eventMapper.toEventFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        userFeignClient.findUserById(userId);
        Event event = findEventWithOutDto(userId, eventId);
        //проверка статуса
        if (event.getState().equals(StateEventDto.PUBLISHED)) {
            throw new ConflictException("Данный Event невозможно изменить, поскольку он уже опубликован");
        } else if (updateEventUserRequest.getStateAction() != null &&
                (event.getState().equals(StateEventDto.CANCELED) &&
                        updateEventUserRequest.getStateAction().equals(StateForUpdateEventDto.SEND_TO_REVIEW))) {
            event.setState(StateEventDto.PENDING);
        } else if (updateEventUserRequest.getStateAction() != null &&
                (event.getState().equals(StateEventDto.PENDING) &&
                        updateEventUserRequest.getStateAction().equals(StateForUpdateEventDto.CANCEL_REVIEW))) {
            event.setState(StateEventDto.CANCELED);
        }
        //проверка даты
        if (updateEventUserRequest.getEventDate() != null && updateEventUserRequest.getEventDate()
                .isAfter(LocalDateTime.now().plusHours(2))) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        //проверка категории и локации
        Event updateEventWithCategoryAndLocation = updateCategoryAndLocation(updateEventUserRequest, event);
        eventMapper.toUpdateEvent(updateEventUserRequest, updateEventWithCategoryAndLocation);
        return eventMapper.toEventFullDto(eventRepository.save(updateEventWithCategoryAndLocation));
    }

    @Override
    public Event findEventById(Long eventId) {
        log.info("Получен запрос на поиск Event с id: [ {} ]", eventId);
        Event event = eventRepository.findEventById(eventId).orElseThrow(() -> {
            log.error("Event c id: [ {} ] не найден", eventId);
            return new NotFoundException("Event c id - " + eventId + " не найден");
        });
        log.debug("Найденный объект Event: {}", event);
        return event;
    }

    @Override
    public EventFullDto findEventByIdForFeign(Long eventId) {
        Event event = findEventById(eventId);
        EventFullDto fullDto = eventMapper.toEventFullDto(event);
        log.debug("Найденный объект EventFullDto: {}", fullDto);
        return fullDto;
    }

    @Transactional
    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto) {
        Event event = findEventById(eventId);
        if (updateEventAdminRequestDto.getEventDate() != null) {
            if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Дата начала изменяемого события должна быть " +
                        "не ранее чем за час от текущего времени. Текущая дата события: " + event.getEventDate());
            }
        }
        if (!event.getState().equals(StateEventDto.PENDING)) {
            throw new ConflictException("Статус у события, которое планируется опубликовать/отклонить, " +
                    "должен быть PENDING. Текущий статус: " + event.getState());
        }
        if (updateEventAdminRequestDto.getStateAction() != null) {
            if (updateEventAdminRequestDto.getStateAction().equals(StateForUpdateEventDto.PUBLISH_EVENT)) {
                event.setState(StateEventDto.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (updateEventAdminRequestDto.getStateAction().equals(StateForUpdateEventDto.REJECT_EVENT)) {
                event.setState(StateEventDto.CANCELED);
            }
        }
        Event updateEventWithCategoryAndLocation = updateCategoryAndLocation(updateEventAdminRequestDto, event);
        eventMapper.toUpdateEvent(updateEventAdminRequestDto, updateEventWithCategoryAndLocation);
        return eventMapper.toEventFullDto(eventRepository.save(updateEventWithCategoryAndLocation));
    }

    @Override
    public List<EventFullDto> findEventByParamsAdmin(EventAdminParamDto eventParamDto) {
        BooleanBuilder booleanBuilder = EventRepository.PredicatesForParamAdmin.build(eventParamDto);

        Pageable pageable = PageRequestUtil.of(eventParamDto.getFrom(),
                eventParamDto.getSize(), Sort.by("id").ascending());

        List<Event> event = eventRepository.findAll(booleanBuilder, pageable).getContent();
        return event.stream().map(eventMapper::toEventFullDto).toList();
    }

    @Override
    public List<EventShortDto> findEventByParamsPublic(EventPublicParamsDto eventPublicParamsDto,
                                                       HttpServletRequest request) {
        if (eventPublicParamsDto.getRangeEnd() != null && eventPublicParamsDto.getRangeStart() != null) {
            if (eventPublicParamsDto.getRangeEnd().isBefore(eventPublicParamsDto.getRangeStart())) {
                throw new IllegalStateException("Дата RangeEnd не должна быть раньше даты RangeStart. RangeStart:" +
                        eventPublicParamsDto.getRangeStart() + ". RangeEnd:" + eventPublicParamsDto.getRangeEnd());
            }
        }

        BooleanBuilder booleanBuilder = EventRepository.PredicatesForParamPublic.build(eventPublicParamsDto);

        String sort;
        if (eventPublicParamsDto.getSort() == null) {
            sort = "id";
        } else if (eventPublicParamsDto.getSort().equals(SortForParamPublicEventDto.EVENT_DATE)) {
            sort = "eventDate";
        } else if (eventPublicParamsDto.getSort().equals(SortForParamPublicEventDto.VIEWS)) {
            sort = "views";
        } else if (eventPublicParamsDto.getSort().equals(SortForParamPublicEventDto.RATING)) {
            sort = "rating";
        } else {
            sort = "id";
        }

        Pageable pageable = PageRequestUtil.of(
                eventPublicParamsDto.getFrom(),
                eventPublicParamsDto.getSize(),
                Sort.by(sort).descending()
        );

        List<Event> events = eventRepository.findAll(booleanBuilder, pageable).getContent();

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    private Event updateCategoryAndLocation(UpdateEventUserRequest updateEventUserRequest, Event event) {
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryMapper.toCategory(
                    categoryService.getCategory(updateEventUserRequest.getCategory()));
            event.setCategory(category);
        }
        if (updateEventUserRequest.getLocation() != null) {
            Location location = locationRepository.save(locationMapper.toLocation(updateEventUserRequest.getLocation()));
            event.setLocation(location);
        }
        return event;
    }

    @Override
    public EventFullDto findPublicEventById(Long userId, Long eventId, HttpServletRequest request) {
        userFeignClient.findUserById(userId);
        Event event = findEventById(eventId);
        if (!event.getState().equals(StateEventDto.PUBLISHED)) {
            throw new NotFoundException("Событие не доступно. Статус события: " + event.getState());
        }

        collectorClient.collectUserAction(UserActionProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setActionType(ActionTypeProto.ACTION_VIEW)
                .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build())
                .build());
        log.debug("Статистика успешно записана");
        EventFullDto fullDto = eventMapper.toEventFullDto(event);
        log.debug("Возвращенный объект при запросе: {}", fullDto);
        return fullDto;
    }

    @Override
    public Event findEventWithOutDto(Long userId, Long eventId) {
        return eventRepository.findEventByUserIdAndEventId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Event c id - " + eventId + " не найден у пользователя с id - " + userId));
    }

    @Override
    public List<Event> findEventsByids(List<Long> eventsIds) {
        List<Event> events = eventRepository.findEventsByIds(eventsIds);
        if (events.isEmpty()) {
            throw new NotFoundException("Events c ids - " + eventsIds + " не найдены");
        } else {
            return events;
        }
    }

    @Transactional
    @Override
    public void saveEventWithRequest(Event event) {
        eventRepository.save(event);
    }

    @Transactional
    @Override
    public void updateConfirmedRequestsFromFeign(Long eventId, EventFullDto eventFullDto) {
        log.info("Получен запрос на обновление статуса через feign client. Id event: {}. Event: {}",
                eventId, eventFullDto);
        Event event = findEventById(eventId);
        //проверяем в полученном dto поле ConfirmedRequests
        if (eventFullDto.getConfirmedRequests() == null || eventFullDto.getConfirmedRequests() < 0) {
            log.error("В переданном объекте некорректно передано поле \"ConfirmedRequests\". {}", eventFullDto);
            throw new ConflictException("В переданном объекте некорректно передано поле \"ConfirmedRequests\". " +
                    eventFullDto);
        }
        event.setConfirmedRequests(eventFullDto.getConfirmedRequests());
        EventFullDto eventUpdate = eventMapper.toEventFullDto(eventRepository.save(event));
        log.debug("Сохраненный объект: {}", eventUpdate);
    }

    @Override
    public List<EventShortDto> getRecommendations(Long userId, int size) {
        log.info("Получен запрос на получения рекомендаций по мероприятиям от пользователя с id: {}", userId);
        userFeignClient.findUserById(userId);
        Map<Long, Double> mapRecommendation = analyzerClient.getRecommendationsForUser(userId, size)
                .collect(Collectors.toMap(
                        RecommendedEventProto::getEventId,
                        proto -> (double) proto.getScore()));

        if (mapRecommendation.isEmpty()) {
            log.debug("отсутствуют рекомендации для пользователя с id: [ {} ]", userId);
            return List.of();
        }

        List<Event> events = eventRepository.findAllById(mapRecommendation.keySet());

        List<EventShortDto> eventShortDtos = events.stream()
                .map(event -> {
                    event.setRating(mapRecommendation.getOrDefault(event.getId(), 0.0));
                    return eventMapper.toEventShortDto(event);
                })
                .sorted(Comparator.comparingDouble(EventShortDto::getRating).reversed())
                .toList();
        log.debug("Рекомендованные мероприятия: {}", eventShortDtos);
        return eventShortDtos;
    }
}
