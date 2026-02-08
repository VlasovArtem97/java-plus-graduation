package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest req);

    List<UserDto> get(List<Long> ids, int from, int size);

    void delete(long userId);

    User findUserById(Long userId);
}
