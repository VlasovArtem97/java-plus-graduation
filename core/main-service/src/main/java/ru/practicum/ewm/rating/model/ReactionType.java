package ru.practicum.ewm.rating.model;

public enum ReactionType {
    LIKE(1),
    DISLIKE(-1);

    private final int score;

    ReactionType(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}

