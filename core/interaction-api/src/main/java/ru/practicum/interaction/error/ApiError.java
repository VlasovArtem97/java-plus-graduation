package ru.practicum.interaction.error;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private String message;
    private String reason;
    private String status;
    private LocalDateTime timestamp;
}