package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repo.UserRepository;
import ru.practicum.ewm.util.PageRequestUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    public User findUserById(Long userId) {
        return repo.findById(userId).orElseThrow(() ->
                new NotFoundException("User with id=" + userId + " was not found"));
    }
}

