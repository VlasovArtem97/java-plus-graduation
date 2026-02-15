package ru.practicum.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.interaction.dto.user.NewUserRequest;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest d);

    UserDto toDto(User u);

    UserShortDto toUserShortDto(User user);
}
