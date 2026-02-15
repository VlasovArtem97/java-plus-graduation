package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.interaction.dto.request.RequestDTO;
import ru.practicum.request.model.Request;

@Mapper(componentModel = "spring")
public interface RequestMapper {

//    @Mapping(source = "request.event.id", target = "eventId")
//    @Mapping(source = "request.requester.id", target = "requesterId")
    RequestDTO toRequestDTO(Request request);

    Request toRequest(RequestDTO requestDTO);
}