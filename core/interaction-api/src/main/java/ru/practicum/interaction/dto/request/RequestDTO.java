package ru.practicum.interaction.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.interaction.dto.request.enums.RequestStatusDto;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {

    private Long id;

    private LocalDateTime created;

    @JsonProperty(value = "event")
    private Long eventId;

    @JsonProperty(value = "requester")
    private Long requesterId;

    @JsonProperty(value = "status")
    private RequestStatusDto requestStatus;
}
