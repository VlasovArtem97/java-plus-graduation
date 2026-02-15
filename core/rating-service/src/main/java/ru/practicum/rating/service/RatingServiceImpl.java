package ru.practicum.rating.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.enums.StateEventDto;
import ru.practicum.interaction.dto.rating.RatingSummaryDto;
import ru.practicum.interaction.dto.rating.enums.ReactionTypeDto;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.error.ConflictException;
import ru.practicum.interaction.error.NotFoundException;
import ru.practicum.interaction.feignclient.EventFeignClient;
import ru.practicum.interaction.feignclient.UserFeignClient;
import ru.practicum.rating.model.EventRating;
import ru.practicum.rating.model.ReactionType;
import ru.practicum.rating.repository.EventRatingRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {

    private final EventRatingRepository ratingRepository;
    private final EventFeignClient eventFeignClient;
    private final UserFeignClient userFeignClient;

    @Transactional
    @Override
    public RatingSummaryDto rate(Long userId, Long eventId, ReactionTypeDto reaction) {
        UserDto userDto = userFeignClient.findUserById(userId);
        EventFullDto event = eventFeignClient.findEventByIdForFeign(eventId);

        if (event.getInitiator() != null && event.getInitiator().equals(userId)) {
            throw new ConflictException("Инициатор не может оценивать своё событие");
        }

        if (event.getState() != StateEventDto.PUBLISHED) {
            throw new ConflictException("Нельзя голосовать за непубликованное событие");
        }

        EventRating rating = ratingRepository.findByEventAndUser(eventId, userId)
                .orElseGet(() -> EventRating.builder()
                        .event(event.getId())
                        .user(userDto.getId())
                        .createdAt(LocalDateTime.now())
                        .build());

        rating.setReaction(ReactionType.valueOf(reaction.name()));
        rating.setValue(reaction.getScore());
        rating.setUpdatedAt(LocalDateTime.now());
        ratingRepository.save(rating);
        return getSummary(eventId);
    }

    @Transactional
    @Override
    public RatingSummaryDto removeRate(Long userId, Long eventId) {
        EventRating rating = ratingRepository.findByEventAndUser(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Реакция не найдена"));
        ratingRepository.delete(rating);
        return getSummary(eventId);
    }

    @Override
    public RatingSummaryDto getSummary(Long eventId) {
        Long likes = ratingRepository.countLikes(eventId);
        Long dislikes = ratingRepository.countDislikes(eventId);
        Long total = ratingRepository.sumScoreByEventId(eventId);
        return RatingSummaryDto.builder()
                .eventId(eventId)
                .likes(likes)
                .dislikes(dislikes)
                .rating(total)
                .build();
    }
}
