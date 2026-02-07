package ru.practicum.ewm.rating.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.event.status.StateEvent;
import ru.practicum.ewm.rating.dto.RatingSummaryDto;
import ru.practicum.ewm.rating.model.EventRating;
import ru.practicum.ewm.rating.model.ReactionType;
import ru.practicum.ewm.rating.repository.EventRatingRepository;
import ru.practicum.ewm.user.service.UserService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {

    private final EventRatingRepository ratingRepository;
    private final EventService eventService;
    private final UserService userService;

    @Transactional
    @Override
    public RatingSummaryDto rate(Long userId, Long eventId, ReactionType reaction) {
        userService.findUserById(userId);
        Event event = eventService.findEventById(eventId);

        if (event.getInitiator() != null && event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор не может оценивать своё событие");
        }

        if (event.getState() != StateEvent.PUBLISHED) {
            throw new ConflictException("Нельзя голосовать за непубликованное событие");
        }

        EventRating rating = ratingRepository.findByEvent_IdAndUser_Id(eventId, userId)
                .orElseGet(() -> EventRating.builder()
                        .event(event)
                        .user(userService.findUserById(userId))
                        .createdAt(LocalDateTime.now())
                        .build());

        rating.setReaction(reaction);
        rating.setValue(reaction.getScore());
        rating.setUpdatedAt(LocalDateTime.now());
        ratingRepository.save(rating);
        return getSummary(eventId);
    }

    @Transactional
    @Override
    public RatingSummaryDto removeRate(Long userId, Long eventId) {
        EventRating rating = ratingRepository.findByEvent_IdAndUser_Id(eventId, userId)
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
