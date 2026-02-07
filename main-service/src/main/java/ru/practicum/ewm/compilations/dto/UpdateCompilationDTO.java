package ru.practicum.ewm.compilations.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationDTO {

    private List<Long> events;

    private Boolean pinned;

    @Size(min = 3, max = 50)
    private String title;
}