package ru.practicum.ewm.rating.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.ewm.rating.model.ReactionType;

@Data
public class RateRequest {
    @NotNull
    private ReactionType reaction;
}
