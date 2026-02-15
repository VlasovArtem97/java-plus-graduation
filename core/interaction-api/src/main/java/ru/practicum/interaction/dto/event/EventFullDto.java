package ru.practicum.interaction.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.event.enums.StateEventDto;
import ru.practicum.interaction.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {

    private Long id;
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    private LocalDateTime createdOn;
    private String description;
    private LocalDateTime eventDate;
//    private UserShortDto initiator;
    private Long initiator;
    private LocationDto location;
    private Boolean paid;
    private Long participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private StateEventDto state;
    private String title;
    private Long views;
    private Long rating;
}
