package ru.practicum.user.service;

import ru.practicum.interaction.dto.user.NewUserRequest;
import ru.practicum.interaction.dto.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest req);

    List<UserDto> get(List<Long> ids, int from, int size);

    void delete(long userId);

    UserDto findUserById(Long userId);
}
