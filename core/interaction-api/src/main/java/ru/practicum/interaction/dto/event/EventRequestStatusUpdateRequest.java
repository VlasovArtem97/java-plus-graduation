package ru.practicum.interaction.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.interaction.dto.request.enums.RequestStatusDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    @NotNull
    private List<Long> requestIds;
    @NotNull
    private RequestStatusDto status;
}
