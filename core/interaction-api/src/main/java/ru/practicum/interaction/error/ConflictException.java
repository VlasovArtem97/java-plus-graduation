package ru.practicum.interaction.error;

public class ConflictException extends RuntimeException {
    public ConflictException(String msg) {
        super(msg);
    }
}
