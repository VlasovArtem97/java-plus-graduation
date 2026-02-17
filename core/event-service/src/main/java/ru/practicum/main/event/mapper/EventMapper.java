package ru.practicum.main.event.mapper;

import org.mapstruct.*;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;
import ru.practicum.interaction.dto.event.enums.StateEventDto;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.Location;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = StateEventDto.class)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "user.id", target = "initiator")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "location", target = "location")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "state", expression = "java(StateEventDto.PENDING)")
    @Mapping(target = "confirmedRequests", constant = "0L")
    @Mapping(target = "views", constant = "0L")
    Event toEvent(NewEventDto newEventDto, UserDto user, Category category, Location location);

    EventFullDto toEventFullDto(Event event);

    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "compilations", ignore = true)
    @Mapping(target = "location.id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toUpdateEvent(UpdateEventUserRequest updateEventUserRequest, @MappingTarget Event event);
}
