package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import ru.practicum.interaction.dto.request.RequestDTO;
import ru.practicum.request.model.Request;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    RequestDTO toRequestDTO(Request request);

    Request toRequest(RequestDTO requestDTO);
}