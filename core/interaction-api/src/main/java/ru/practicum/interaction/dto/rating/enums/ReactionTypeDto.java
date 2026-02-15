package ru.practicum.interaction.dto.rating.enums;

public enum ReactionTypeDto {
    LIKE(1),
    DISLIKE(-1);

    private final int score;

    ReactionTypeDto(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}

