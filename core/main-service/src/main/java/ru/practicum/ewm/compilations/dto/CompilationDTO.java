package ru.practicum.ewm.compilations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDTO {

    private Long id;

    private List<EventShortDto> events;

    private Boolean pinned;

    @NotBlank(message = "Подборка событий должна иметь название")
    @Size(min = 3, max = 50)
    private String title;
}
