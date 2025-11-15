package dev.botnavas.tgspentbot.config.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConfigException extends RuntimeException {
    public ConfigException(String message) {
        super(message);
    }
}
