package dev.botnavas.tgspentbot.exception.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConfigException extends RuntimeException {
    public ConfigException(String message) {
        super(message);
    }
}
