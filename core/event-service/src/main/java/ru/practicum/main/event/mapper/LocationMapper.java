package ru.practicum.main.event.mapper;

import org.mapstruct.Mapper;
import ru.practicum.interaction.dto.event.LocationDto;
import ru.practicum.main.event.model.Location;


@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location toLocation(LocationDto location);

    LocationDto toLocationDto(Location location);
}
