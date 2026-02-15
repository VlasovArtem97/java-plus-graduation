package ru.practicum.interaction.dto.event.enums;

import java.util.Optional;

public enum SortForParamPublicEventDto {
    EVENT_DATE,
    VIEWS,
    RATING;

    public static Optional<SortForParamPublicEventDto> from(String stringSort) {
        for (SortForParamPublicEventDto sort : values()) {
            if (sort.name().equalsIgnoreCase(stringSort)) {

                return Optional.of(sort);
            }
        }
        return Optional.empty();
    }
}
