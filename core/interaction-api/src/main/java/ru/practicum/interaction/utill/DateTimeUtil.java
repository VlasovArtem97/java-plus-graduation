package ru.practicum.interaction.utill;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class DateTimeUtil {

    public static Boolean isValidStartAndEnd(LocalDateTime start, LocalDateTime end) {
        return start.isBefore(end);
    }
}
