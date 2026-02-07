package ru.practicum.ewm.event.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.korshunov.statsclient.StatsClient;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.event.status.SortForParamPublicEvent;
import ru.practicum.ewm.event.status.StateEvent;
import ru.practicum.ewm.event.status.StateForUpdateEvent;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.ewm.util.PageRequestUtil;
import statsdto.HitDto;
import statsdto.StatDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final UserService userService;
    private final EventMapper eventMapper;
    private final CategoryMapper categoryMapper;
    private final LocationMapper locationMapper;
    private final CategoryService categoryService;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;


    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userService.findUserById(userId);
        Category category = categoryMapper.toCategory(categoryService.getCategory(newEventDto.getCategory()));
        Location location = locationRepository.save(locationMapper.toLocation(newEventDto.getLocation()));
        Event event = eventRepository.save(eventMapper.toEvent(newEventDto, user, category, location));
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> findEventByUserId(Long userId, int from, int size) {
        userService.findUserById(userId);
        Pageable pageable = PageRequestUtil.of(from, size, Sort.by("id").ascending());
        return eventRepository.findEventByUserId(userId, pageable).getContent().stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto findEventByIdAndEventId(Long userId, Long eventId) {
        userService.findUserById(userId);
        Event event = findEventWithOutDto(userId, eventId);
        List<Event> eventsWithView = getStats(List.of(event), null, null, true);
        return eventMapper.toEventFullDto(eventsWithView.getFirst());
    }

    @Transactional
    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        userService.findUserById(userId);
        Event event = findEventWithOutDto(userId, eventId);
        //проверка статуса
        if (event.getState().equals(StateEvent.PUBLISHED)) {
            throw new ConflictException("Данный Event невозможно изменить, поскольку он уже опубликован");
        } else if (updateEventUserRequest.getStateAction() != null &&
                (event.getState().equals(StateEvent.CANCELED) &&
                        updateEventUserRequest.getStateAction().equals(StateForUpdateEvent.SEND_TO_REVIEW))) {
            event.setState(StateEvent.PENDING);
        } else if (updateEventUserRequest.getStateAction() != null &&
                (event.getState().equals(StateEvent.PENDING) &&
                        updateEventUserRequest.getStateAction().equals(StateForUpdateEvent.CANCEL_REVIEW))) {
            event.setState(StateEvent.CANCELED);
        }
        //проверка даты
        if (updateEventUserRequest.getEventDate() != null && updateEventUserRequest.getEventDate()
                .isAfter(LocalDateTime.now().plusHours(2))) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        //проверка категории и локации
        Event updateEventWithCategoryAndLocation = updateCategoryAndLocation(updateEventUserRequest, event);
        List<Event> eventWithView = getStats(List.of(updateEventWithCategoryAndLocation), null, null, true);
        eventMapper.toUpdateEvent(updateEventUserRequest, eventWithView.getFirst());
        return eventMapper.toEventFullDto(eventRepository.save(eventWithView.getFirst()));
    }

    @Override
    public Event findEventById(Long eventId) {
        return eventRepository.findEventById(eventId).orElseThrow(() ->
                new NotFoundException("Event c id - " + eventId + " не найден"));
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
        if (!event.getState().equals(StateEvent.PENDING)) {
            throw new ConflictException("Статус у события, которое планируется опубликовать/отклонить, " +
                    "должен быть PENDING. Текущий статус: " + event.getState());
        }
        if (updateEventAdminRequestDto.getStateAction() != null) {
            if (updateEventAdminRequestDto.getStateAction().equals(StateForUpdateEvent.PUBLISH_EVENT)) {
                event.setState(StateEvent.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (updateEventAdminRequestDto.getStateAction().equals(StateForUpdateEvent.REJECT_EVENT)) {
                event.setState(StateEvent.CANCELED);
            }
        }
        Event updateEventWithCategoryAndLocation = updateCategoryAndLocation(updateEventAdminRequestDto, event);
        List<Event> eventWithView = getStats(List.of(updateEventWithCategoryAndLocation), null, null, true);
        eventMapper.toUpdateEvent(updateEventAdminRequestDto, eventWithView.getFirst());
        return eventMapper.toEventFullDto(eventRepository.save(eventWithView.getFirst()));
    }

    @Override
    public List<EventFullDto> findEventByParamsAdmin(EventAdminParamDto eventParamDto) {
        BooleanBuilder booleanBuilder = EventRepository.PredicatesForParamAdmin.build(eventParamDto);

        Pageable pageable = PageRequestUtil.of(eventParamDto.getFrom(),
                eventParamDto.getSize(), Sort.by("id").ascending());

        List<Event> event = eventRepository.findAll(booleanBuilder, pageable).getContent();
        List<Event> eventWithView = getStats(event, null, null, true);
        return eventWithView.stream().map(eventMapper::toEventFullDto).toList();
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
        } else if (eventPublicParamsDto.getSort().equals(SortForParamPublicEvent.EVENT_DATE)) {
            sort = "eventDate";
        } else if (eventPublicParamsDto.getSort().equals(SortForParamPublicEvent.VIEWS)) {
            sort = "views";
        } else if (eventPublicParamsDto.getSort().equals(SortForParamPublicEvent.RATING)) {
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
        List<Event> eventsWithViews = getStats(events, null, null, true);
        addViewEvent(request);

        return eventsWithViews.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }


    @Transactional
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
    public EventFullDto findPublicEventById(Long eventId, HttpServletRequest request) {
        Event event = findEventById(eventId);
        if (!event.getState().equals(StateEvent.PUBLISHED)) {
            throw new NotFoundException("Событие не доступно. Статус события: " + event.getState());
        }
        List<Event> eventWithView = getStats(List.of(event), null, null, true);
        addViewEvent(request);
        return eventMapper.toEventFullDto(eventWithView.getFirst());
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
            return getStats(events, null, null, true);
        }
    }

    @Override
    public void saveEventWithRequest(Event event) {
        eventRepository.save(event);
    }

    //Добавил в параметры: время и уникальность. "Если проект будет расширяться"
    private List<Event> getStats(List<Event> events, LocalDateTime start, LocalDateTime end, Boolean unique) {
        Map<Long, String> eventsUri = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> "/events/" + e.getId()
                ));
        LocalDateTime rangeStart = Objects.requireNonNullElseGet(start, () ->
                LocalDateTime.of(2025, 1, 1, 1, 1, 1));
        LocalDateTime rangeEnd = Objects.requireNonNullElseGet(end, () ->
                LocalDateTime.of(2050, 1, 1, 1, 1, 1));
        Boolean uni = Objects.requireNonNullElseGet(unique, () -> false);

        List<StatDto> statDtos = statsClient.getStats(rangeStart, rangeEnd, eventsUri.values().stream().toList(), uni);

        Map<Long, StatDto> statDtoMap = statDtos.stream()
                .filter(stat -> stat.getUri()
                        .substring(stat.getUri().lastIndexOf("/") + 1).matches("\\d+"))
                .collect(Collectors.toMap(
                        stat -> Long.parseLong(stat.getUri().substring(stat.getUri().lastIndexOf("/") + 1)),
                        stat -> stat
                ));
        for (Event event : events) {
            StatDto view = statDtoMap.get(event.getId());
            event.setViews(view != null ? view.getHits() : 0L);
        }
        return List.copyOf(events);
    }

    private void addViewEvent(HttpServletRequest httpServletRequest) {
        statsClient.addHit(HitDto.builder()
                .app("ewm-main-service")
                .uri(httpServletRequest.getRequestURI())
                .ip(httpServletRequest.getRemoteAddr())
                .build());
    }
}
