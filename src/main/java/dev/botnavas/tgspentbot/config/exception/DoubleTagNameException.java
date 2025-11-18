package dev.botnavas.tgspentbot.config.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DoubleTagNameException extends RuntimeException {
    public DoubleTagNameException(String message) {
        super(message);
    }
}