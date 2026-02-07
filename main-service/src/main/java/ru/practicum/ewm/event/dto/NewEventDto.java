package ru.practicum.ewm.event.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.validatelocaldatetime.DateTimeValidate;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @Positive
    @NotNull
    private Long category;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    @DateTimeValidate
    @Future
    private LocalDateTime eventDate;

    @NotNull
    @Valid
    private LocationDto location;

    @Builder.Default
    private Boolean paid = false;

    @PositiveOrZero
    @Builder.Default
    private Long participantLimit = 0L;

    @Builder.Default
    private Boolean requestModeration = true;

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
}
