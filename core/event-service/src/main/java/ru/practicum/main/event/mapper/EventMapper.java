package ru.practicum.main.event.mapper;

import org.mapstruct.*;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.Location;
import ru.practicum.main.event.model.status.StateEvent;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @BeanMapping(qualifiedByName = "event")
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "user.id", target = "initiator")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "location", target = "location")
    Event toEvent(NewEventDto newEventDto, /* User */UserDto user, Category category, Location location);


    @Named("event")
    @AfterMapping
    default void setDefaultCreatedOn(@MappingTarget Event.EventBuilder event) {
        event.createdOn(LocalDateTime.now());
        event.state(StateEvent.PENDING);
        event.confirmedRequests(0L);
        event.views(0L);
//        event.rating(0L);
    }

    EventFullDto toEventFullDto(Event event);

    EventShortDto toEventShortDto(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, qualifiedByName = "updateEvent")
    @Mapping(target = "category", ignore = true)
    void toUpdateEvent(UpdateEventUserRequest updateEventUserRequest, @MappingTarget Event event);
}
