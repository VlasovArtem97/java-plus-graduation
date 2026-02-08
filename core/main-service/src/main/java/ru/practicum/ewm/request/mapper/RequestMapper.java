package ru.practicum.ewm.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.request.dto.RequestDTO;
import ru.practicum.ewm.request.model.Request;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(source = "request.event.id", target = "eventId")
    @Mapping(source = "request.requester.id", target = "requesterId")
    RequestDTO toRequestDTO(Request request);
}