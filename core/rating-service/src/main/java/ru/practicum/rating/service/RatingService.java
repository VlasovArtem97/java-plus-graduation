package ru.practicum.rating.service;

import ru.practicum.interaction.dto.rating.RatingSummaryDto;
import ru.practicum.interaction.dto.rating.enums.ReactionTypeDto;
import ru.practicum.rating.model.ReactionType;

public interface RatingService {
    RatingSummaryDto rate(Long userId, Long eventId, ReactionTypeDto reaction);

    RatingSummaryDto removeRate(Long userId, Long eventId);

    RatingSummaryDto getSummary(Long eventId);
}
