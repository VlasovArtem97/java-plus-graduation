package ru.practicum.ewm.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RatingSummaryDto {
    private Long eventId;
    private Long likes;
    private Long dislikes;
    private Long rating;
}
