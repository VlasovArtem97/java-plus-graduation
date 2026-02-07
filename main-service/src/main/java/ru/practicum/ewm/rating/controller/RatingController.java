package ru.practicum.ewm.rating.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.rating.dto.RateRequest;
import ru.practicum.ewm.rating.dto.RatingSummaryDto;
import ru.practicum.ewm.rating.service.RatingService;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/users/{userId}/events/{eventId}/rating")
    public RatingSummaryDto rate(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 @RequestBody @Valid RateRequest request) {
        return ratingService.rate(userId, eventId, request.getReaction());
    }

    @DeleteMapping("/users/{userId}/events/{eventId}/rating")
    public RatingSummaryDto remove(@PathVariable Long userId, @PathVariable Long eventId) {
        return ratingService.removeRate(userId, eventId);
    }

    @GetMapping("/events/{eventId}/rating")
    public RatingSummaryDto get(@PathVariable Long eventId) {
        return ratingService.getSummary(eventId);
    }
}
