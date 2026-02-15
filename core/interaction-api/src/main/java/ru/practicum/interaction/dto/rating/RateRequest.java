package ru.practicum.interaction.dto.rating;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.interaction.dto.rating.enums.ReactionTypeDto;

@Data
public class RateRequest {
    @NotNull
    private ReactionTypeDto reaction;
}
