package ru.practicum.interaction.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.interaction.dto.request.RequestDTO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateResult {

    private List<RequestDTO> confirmedRequests;
    private List<RequestDTO> rejectedRequests;
}
