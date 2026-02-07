package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.event.status.StateForUpdateEvent;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UpdateEventAdminRequestDto extends UpdateEventUserRequest {

    @Size(min = 20, max = 2000)
    private String annotation;

    @Positive
    private Long category;

    @Size(min = 20, max = 7000)
    private String description;

    @Future
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero
    private Long participantLimit;

    private Boolean requestModeration;

    private StateForUpdateEvent stateAction;

    @Size(min = 3, max = 120)
    private String title;
}
