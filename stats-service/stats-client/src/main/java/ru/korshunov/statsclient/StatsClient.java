package ru.korshunov.statsclient;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.validation.annotation.Validated;
import statsdto.HitDto;
import statsdto.StatDto;

import java.time.LocalDateTime;
import java.util.List;

@Validated
public interface StatsClient {

    void addHit(@Valid HitDto hitDto);

    List<StatDto> getStats(@NotNull(message = "Дата начала не долна быть пустой")
                           @Past(message = "Дата начала должна быть в прошлом")
                           LocalDateTime start,
                           @NotNull(message = "Дата конца не долна быть пустой")
                           @PastOrPresent(message = "Дата конца не может быть в будущем")
                           LocalDateTime end);

    List<StatDto> getStats(@NotNull(message = "Дата начала не долна быть пустой")
                           @Past(message = "Дата начала должна быть в прошлом")
                           LocalDateTime start,
                           @NotNull(message = "Дата конца не долна быть пустой")
                           @PastOrPresent(message = "Дата конца не может быть в будущем")
                           LocalDateTime end,
                           @NotNull(message = "Отсутствует список URI")
                           List<String> uris);

    List<StatDto> getStats(@NotNull(message = "Дата начала не долна быть пустой")
                           @Past(message = "Дата начала должна быть в прошлом")
                           LocalDateTime start,
                           @NotNull(message = "Дата конца не долна быть пустой")
                           @PastOrPresent(message = "Дата конца не может быть в будущем")
                           LocalDateTime end,
                           @NotNull(message = "Отсутствует флаг уникальности")
                           Boolean unique);

    List<StatDto> getStats(@NotNull(message = "Дата начала не долна быть пустой")
                           LocalDateTime start,
                           @NotNull(message = "Дата конца не долна быть пустой")
                           LocalDateTime end,
                           @NotNull(message = "Отсутствует список URI")
                           List<String> uris,
                           @NotNull(message = "Отсутствует флаг уникальности")
                           Boolean unique);
}