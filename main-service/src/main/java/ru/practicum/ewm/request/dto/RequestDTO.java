package ru.practicum.ewm.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.request.model.RequestStatus;

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
    private RequestStatus requestStatus;
}
