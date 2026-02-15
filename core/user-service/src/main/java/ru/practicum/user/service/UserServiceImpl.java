package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.dto.user.NewUserRequest;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.error.ConflictException;
import ru.practicum.interaction.error.NotFoundException;
import ru.practicum.interaction.utill.PageRequestUtil;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repo.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(NewUserRequest req) {
        if (repo.existsByEmailIgnoreCase(req.getEmail()))
            throw new ConflictException("User with email already exists: " + req.getEmail());
        return userMapper.toDto(repo.save(userMapper.toEntity(req)));
    }

    @Override
    public List<UserDto> get(List<Long> ids, int from, int size) {
        Pageable pr = PageRequestUtil.of(from, size, Sort.by("id").ascending());
        var page = (ids == null || ids.isEmpty())
                ? repo.findAll(pr)
                : repo.findAllByIdIn(ids, pr);
        return page.map(userMapper::toDto).getContent();
    }

    @Override
    @Transactional
    public void delete(long userId) {
        if (!repo.existsById(userId)) throw new NotFoundException("User with id=" + userId + " was not found");
        repo.deleteById(userId);
    }

    @Override
    public UserDto findUserById(Long userId) {
        log.info("Получен запрос на поиск пользователя с id: {}", userId);
        User user = repo.findById(userId).orElseThrow(() -> {
            log.error("Пользователь с id: [ {} ] не найден", userId);
            return new NotFoundException("User with id=" + userId + " was not found");
        });
        UserDto userDto = userMapper.toDto(user);
        log.debug("Найденный пользователь: {}", userDto);
        return userDto;
    }
}

