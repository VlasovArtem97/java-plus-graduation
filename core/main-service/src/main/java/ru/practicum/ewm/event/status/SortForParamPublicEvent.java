package ru.practicum.ewm.event.status;

import java.util.Optional;

public enum SortForParamPublicEvent {
    EVENT_DATE,
    VIEWS,
    RATING;

    public static Optional<SortForParamPublicEvent> from(String stringSort) {
        for (SortForParamPublicEvent sort : values()) {
            if (sort.name().equalsIgnoreCase(stringSort)) {

                return Optional.of(sort);
            }
        }
        return Optional.empty();
    }
}
