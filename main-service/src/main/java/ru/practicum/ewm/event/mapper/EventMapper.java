package ru.practicum.ewm.event.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.status.StateEvent;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @BeanMapping(qualifiedByName = "event")
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "user", target = "initiator")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "location", target = "location")
    Event toEvent(NewEventDto newEventDto, User user, Category category, Location location);


    @Named("event")
    @AfterMapping
    default void setDefaultCreatedOn(@MappingTarget Event.EventBuilder event) {
        event.createdOn(LocalDateTime.now());
        event.state(StateEvent.PENDING);
        event.confirmedRequests(0L);
        event.views(0L);
        event.rating(0L);
    }

    EventFullDto toEventFullDto(Event event);

    EventShortDto toEventShortDto(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, qualifiedByName = "updateEvent")
    @Mapping(target = "category", ignore = true)
    void toUpdateEvent(UpdateEventUserRequest updateEventUserRequest, @MappingTarget Event event);
}
